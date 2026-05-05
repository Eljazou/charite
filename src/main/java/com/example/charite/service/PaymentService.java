package com.example.charite.service;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor  // ← génère le constructeur pour payPalHttpClient
public class PaymentService {

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    private final PayPalHttpClient payPalHttpClient;  // ← injecté via constructeur

    public String createStripeSession(Long actionId, BigDecimal amount, String currency) throws Exception {
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/actions/" + actionId + "/donate/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:8080/actions/" + actionId + "/donate/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Don pour action de charité")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public String createPayPalOrder(Long actionId, BigDecimal amount, String locale) throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        AmountWithBreakdown amountBreakdown = new AmountWithBreakdown()
                .currencyCode("USD")
                .value(amount.setScale(2).toPlainString());

        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                .amountWithBreakdown(amountBreakdown);

        orderRequest.purchaseUnits(List.of(purchaseUnit));

        ApplicationContext appContext = new ApplicationContext()
                .returnUrl("http://localhost:8080/actions/" + actionId + "/donate/paypal/success")
                .cancelUrl("http://localhost:8080/actions/" + actionId + "/donate/cancel")
                .userAction("PAY_NOW");

        orderRequest.applicationContext(appContext);

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(orderRequest);

        HttpResponse<Order> response = payPalHttpClient.execute(request);

        // Récupère l'URL d'approbation
        String approvalUrl = response.result().links().stream()
                .filter(l -> "approve".equals(l.rel()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Lien PayPal introuvable"))
                .href();

        // Force la langue directement dans l'URL
        return approvalUrl + "&locale.x=" + locale;
    }

    public void capturePayPalOrder(String orderId) throws Exception {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        request.prefer("return=representation");
        payPalHttpClient.execute(request);
    }
}