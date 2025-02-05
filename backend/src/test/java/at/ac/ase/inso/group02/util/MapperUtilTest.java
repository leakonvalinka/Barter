package at.ac.ase.inso.group02.util;

import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserInfoDTO;
import at.ac.ase.inso.group02.entities.*;
import at.ac.ase.inso.group02.skills.dto.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@QuarkusTest
public class MapperUtilTest {

    private final String EMAIL = "my@email.com";
    private final String USERNAME = "myUserName";
    private final String DISPLAY_NAME = "myDisplayName";

    @Test
    public void testMapperUtilForUser_ShouldReturnValidDTO() {
        User myUser = getUser();

        UserInfoDTO userInfoDTO = MapperUtil.map(myUser, UserInfoDTO.class);

        Assertions.assertEquals(EMAIL, userInfoDTO.getEmail());
        Assertions.assertEquals(USERNAME, userInfoDTO.getUsername());
        Assertions.assertEquals(DISPLAY_NAME, userInfoDTO.getDisplayName());
    }

    private User getUser() {
        return User.builder()
                .email(EMAIL)
                .username(USERNAME)
                .displayName(DISPLAY_NAME)
                .build();
    }


    @Test
    public void testMapperUtilForUserInfoDTO_ShouldReturnValidUser() {
        UserDetailDTO myUserDTO = getUserDetailDTO();

        User user = MapperUtil.map(myUserDTO, User.class);

        Assertions.assertEquals(EMAIL, user.getEmail());
        Assertions.assertEquals(USERNAME, user.getUsername());
        Assertions.assertEquals(DISPLAY_NAME, user.getDisplayName());
    }

    private UserDetailDTO getUserDetailDTO() {
        return UserDetailDTO.builder()
                .email(EMAIL)
                .username(USERNAME)
                .displayName(DISPLAY_NAME)
                .build();
    }

    @Test
    public void testMapperUtilForSkillSet_ShouldReturnValidDTO() {

        Set<Skill> skills = new HashSet<>();

        skills.add(SkillDemand.builder()
                .id(1L)
                .title("Demand 1")
                .byUser(getUser())
                .category(getSkillCategory())
                .description("Demand 1 Description")
                .urgency(DemandUrgency.HIGH)
                .build()
        );

        SkillCollectionDTO skillCollectionDTO = SkillCollectionDTO.builder().skills(skills.stream().map(s -> MapperUtil.map(s, SkillDTO.class)).toList()).build();

        Assertions.assertInstanceOf(SkillDemandDTO.class, skillCollectionDTO.getSkills().getFirst());
    }


    @Test
    public void testMapperUtilForCreateSkillDemandDTO_ShouldReturnValidEntitiy() {

        CreateSkillDemandDTO createSkillDemandDTO = CreateSkillDemandDTO.builder()
                .title("Demand 1")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(1L).build())
                .description("Demand 1 Description")
                .urgency(DemandUrgency.HIGH)
                .build();

        Skill skill = MapperUtil.map(createSkillDemandDTO, Skill.class);
        Assertions.assertInstanceOf(SkillDemand.class, skill);

        CreateSkillOfferDTO createSkillOfferDTO = CreateSkillOfferDTO.builder()
                .title("Demand 1")
                .category(CreateSkillDTO.SkillCreateCategoryDTO.builder().id(1L).build())
                .description("Demand 1 Description")
                .schedule("My Schedule")
                .build();

        skill = MapperUtil.map(createSkillOfferDTO, Skill.class);
        Assertions.assertInstanceOf(SkillOffer.class, skill);
    }

    @Test
    public void testMapperUtilForSkillDemandDTO_ShouldReturnValidEntities() {
        SkillCollectionDTO skillCollectionDTO = SkillCollectionDTO.builder()
                .skills(List.of(
                        SkillDemandDTO.builder()
                                .id(1L)
                                .title("Demand 1")
                                .byUser(getUserDetailDTO())
                                .category(getSkillCategoryDTO())
                                .description("Demand 1 Description")
                                .urgency(DemandUrgency.HIGH)
                                .build()
                ))
                .build();

        Skill skill = MapperUtil.map(skillCollectionDTO.getSkills().getFirst(), Skill.class);
        Assertions.assertInstanceOf(SkillDemand.class, skill);
    }

    private static SkillCategory getSkillCategory() {
        return SkillCategory.builder()
                .id(1L)
                .name("Category 1")
                .description("Category Description 1")
                .build();
    }

    private static SkillCategoryDTO getSkillCategoryDTO() {
        return SkillCategoryDTO.builder()
                .id(1L)
                .name("Category 1")
                .description("Category Description 1")
                .build();
    }
}
