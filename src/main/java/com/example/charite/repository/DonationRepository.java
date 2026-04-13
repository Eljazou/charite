package com.example.charite.repository;

import com.example.charite.entity.Donation;
import com.example.charite.entity.CharityAction;
import com.example.charite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.charite.entity.Organization;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByCharityAction(CharityAction charityAction);
    @Query("SELECT d FROM Donation d WHERE d.charityAction.organization = :org ORDER BY d.donationDate DESC")
    List<Donation> findRecentByOrganization(@Param("org") Organization org);
    List<Donation> findByUser(User user);
}