package com.foodrescue.config;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.foodrescue.entity.AppRole;
import com.foodrescue.entity.DonorProfile;
import com.foodrescue.entity.DonorType;
import com.foodrescue.entity.MembershipStatus;
import com.foodrescue.entity.NgoMembership;
import com.foodrescue.entity.NgoOrganization;
import com.foodrescue.entity.OrganizationStatus;
import com.foodrescue.entity.UserAccount;
import com.foodrescue.entity.UserStatus;
import com.foodrescue.entity.VolunteerAvailability;
import com.foodrescue.entity.VolunteerProfile;
import com.foodrescue.repository.DonorProfileRepository;
import com.foodrescue.repository.NgoMembershipRepository;
import com.foodrescue.repository.NgoOrganizationRepository;
import com.foodrescue.repository.UserAccountRepository;
import com.foodrescue.repository.VolunteerProfileRepository;

@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final NgoOrganizationRepository ngoOrganizationRepository;
    private final NgoMembershipRepository ngoMembershipRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(
        UserAccountRepository userAccountRepository,
        DonorProfileRepository donorProfileRepository,
        NgoOrganizationRepository ngoOrganizationRepository,
        NgoMembershipRepository ngoMembershipRepository,
        VolunteerProfileRepository volunteerProfileRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.ngoOrganizationRepository = ngoOrganizationRepository;
        this.ngoMembershipRepository = ngoMembershipRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDonor();
        seedNgo();
        seedVolunteer();
        seedAdmin();
    }

    private void seedDonor() {
        String email = "donor@bistro.com";
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFirstName("Elena");
        user.setLastName("Rostova");
        user.setPhone("+1-555-0192");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(Set.of(AppRole.DONOR)));
        UserAccount savedUser = userAccountRepository.save(user);

        DonorProfile donor = new DonorProfile();
        donor.setUser(savedUser);
        donor.setOrganizationName("Green Earth Bistro");
        donor.setOrganizationType(DonorType.RESTAURANT);
        donor.setAddressLine1("742 Evergreen Terrace");
        donor.setCity("Springfield");
        donor.setPostalCode("97477");
        donor.setCountryCode("US");
        donorProfileRepository.save(donor);
    }

    private void seedNgo() {
        String email = "coordinator@shelter.org";
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFirstName("Marcus");
        user.setLastName("Vance");
        user.setPhone("+1-555-0144");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(Set.of(AppRole.NGO)));
        UserAccount savedUser = userAccountRepository.save(user);

        NgoOrganization org = new NgoOrganization();
        org.setName("Hope Community Kitchen");
        org.setRegistrationNumber("NGO-2024-8831");
        org.setContactEmail("contact@hopekitchen.org");
        org.setContactPhone("+1-555-0199");
        org.setAddressLine1("120 Harbor Boulevard");
        org.setCity("Springfield");
        org.setPostalCode("97477");
        org.setCountryCode("US");
        org.setVerificationStatus(OrganizationStatus.VERIFIED);
        NgoOrganization savedOrg = ngoOrganizationRepository.save(org);

        NgoMembership membership = new NgoMembership();
        membership.setOrganization(savedOrg);
        membership.setUser(savedUser);
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setJoinedAt(Instant.now());
        ngoMembershipRepository.save(membership);
    }

    private void seedVolunteer() {
        String email = "volunteer@rescue.org";
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFirstName("Sarah");
        user.setLastName("Connor");
        user.setPhone("+1-555-0188");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(Set.of(AppRole.VOLUNTEER)));
        UserAccount savedUser = userAccountRepository.save(user);

        VolunteerProfile volunteer = new VolunteerProfile();
        volunteer.setUser(savedUser);
        volunteer.setAvailabilityStatus(VolunteerAvailability.AVAILABLE);
        volunteerProfileRepository.save(volunteer);
    }

    private void seedAdmin() {
        String email = "admin@foodrescue.org";
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFirstName("Super");
        user.setLastName("Admin");
        user.setPhone("+1-555-0100");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(Set.of(AppRole.ADMIN)));
        userAccountRepository.save(user);
    }
}
