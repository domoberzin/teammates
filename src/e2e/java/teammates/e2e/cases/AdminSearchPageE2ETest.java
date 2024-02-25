package teammates.e2e.cases;

import java.time.Instant;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import teammates.common.util.AppUrl;
import teammates.common.util.Const;
import teammates.e2e.pageobjects.AdminSearchPage;
import teammates.e2e.util.TestProperties;
import teammates.storage.sqlentity.AccountRequest;
import teammates.storage.sqlentity.Course;
import teammates.storage.sqlentity.FeedbackSession;
import teammates.storage.sqlentity.Instructor;
import teammates.storage.sqlentity.Student;

/**
 * SUT: {@link Const.WebPageURIs#ADMIN_SEARCH_PAGE}.
 */
public class AdminSearchPageE2ETest extends BaseE2ETestCase {

    @Override
    protected void prepareTestData() {
        if (!TestProperties.INCLUDE_SEARCH_TESTS) {
            return;
        }
        testData = loadDataBundle("/AdminSearchPageE2ETest.json");
        sqlTestData = removeAndRestoreSqlDataBundle(
            loadSqlDataBundle("/AdminSearchPageE2ETest_SQLEntities.json"));
        putDocumentsSql(sqlTestData);
        removeAndRestoreDataBundle(testData);
        putDocuments(testData);
    }

    @Test
    @Override
    public void testAll() {
        if (!TestProperties.INCLUDE_SEARCH_TESTS) {
            return;
        }

        AppUrl url = createFrontendUrl(Const.WebPageURIs.ADMIN_SEARCH_PAGE);
        AdminSearchPage searchPage = loginAdminToPage(url, AdminSearchPage.class);

        Course course = sqlTestData.courses.get("typicalCourse1");
        Student student = sqlTestData.students.get("student1InCourse1");
        Instructor instructor = sqlTestData.instructors.get("instructor1OfCourse1");
        AccountRequest accountRequest = sqlTestData.accountRequests.get("unregisteredInstructor1");

        ______TS("Typical case: Search student email");
        String searchContent = student.getEmail();
        searchPage.inputSearchContent(searchContent);
        searchPage.clickSearchButton();
        String studentDetails = getExpectedStudentDetails(student);
        String studentManageAccountLink = getExpectedStudentManageAccountLink(student);
        String studentHomePageLink = getExpectedStudentHomePageLink(student);
        int numExpandedRows = getExpectedNumExpandedRows(student);
        searchPage.verifyStudentRowContent(student, course, studentDetails, studentManageAccountLink,
                studentHomePageLink);
        searchPage.verifyStudentExpandedLinks(student, numExpandedRows);

        // Wait for update student action to be merged
        // ______TS("Typical case: Reset student google id");
        // searchPage.resetStudentGoogleId(student);
        // student.setGoogleId(null);
        // studentManageAccountLink = getExpectedStudentManageAccountLink(student);
        // studentHomePageLink = getExpectedStudentHomePageLink(student);
        // searchPage.verifyStudentRowContent(student, course, studentDetails, studentManageAccountLink,
        //         studentHomePageLink);

        ______TS("Typical case: Regenerate registration key for a course student");
        searchPage.clickExpandStudentLinks();
        String originalJoinLink = searchPage.getStudentJoinLink(student);

        searchPage.regenerateStudentKey(student);
        searchPage.verifyRegenerateStudentKey(student, originalJoinLink);
        searchPage.waitForPageToLoad();

        ______TS("Typical case: Search for instructor email");
        searchPage.clearSearchBox();
        searchContent = instructor.getEmail();
        searchPage.inputSearchContent(searchContent);
        searchPage.clickSearchButton();
        String instructorManageAccountLink = getExpectedInstructorManageAccountLink(instructor);
        String instructorHomePageLink = getExpectedInstructorHomePageLink(instructor);
        searchPage.verifyInstructorRowContent(instructor, course, instructorManageAccountLink,
                instructorHomePageLink);
        searchPage.verifyInstructorExpandedLinks(instructor);

        // ______TS("Typical case: Reset instructor google id");
        // searchPage.resetInstructorGoogleId(instructor);
        // instructor.setGoogleId(null);
        // instructorManageAccountLink = getExpectedInstructorManageAccountLink(instructor);
        // instructorHomePageLink = getExpectedInstructorHomePageLink(instructor);
        // searchPage.verifyInstructorRowContent(instructor, course, instructorManageAccountLink,
        //         instructorHomePageLink);

        ______TS("Typical case: Regenerate registration key for an instructor");
        searchPage.clickExpandInstructorLinks();
        originalJoinLink = searchPage.getInstructorJoinLink(instructor);

        searchPage.regenerateInstructorKey(instructor);
        searchPage.verifyRegenerateInstructorKey(instructor, originalJoinLink);
        searchPage.waitForPageToLoad();

        ______TS("Typical case: Search for account request by email");
        searchPage.clearSearchBox();
        searchContent = accountRequest.getEmail();
        searchPage.inputSearchContent(searchContent);
        searchPage.clickSearchButton();
        searchPage.verifyAccountRequestRowContent(accountRequest);
        searchPage.verifyAccountRequestExpandedLinks(accountRequest);

        ______TS("Typical case: Search common search key");
        searchPage.clearSearchBox();
        searchContent = "Course1";
        searchPage.inputSearchContent(searchContent);
        searchPage.clickSearchButton();
        searchPage.verifyStudentRowContent(student, course, studentDetails, studentManageAccountLink,
                studentHomePageLink);
        searchPage.verifyInstructorRowContent(instructor, course, instructorManageAccountLink,
                instructorHomePageLink);
        searchPage.verifyAccountRequestRowContent(accountRequest);

        ______TS("Typical case: Expand and collapse links");
        searchPage.verifyLinkExpansionButtons(student, instructor, accountRequest);

        ______TS("Typical case: Reset account request successful");
        searchContent = "unregisteredinstructor1";
        searchPage.clearSearchBox();
        searchPage.inputSearchContent(searchContent);
        searchPage.clickSearchButton();
        searchPage.clickResetAccountRequestButton(accountRequest);
        assertNull(BACKDOOR.getAccountRequest(accountRequest.getEmail(), accountRequest.getInstitute()).getRegisteredAt());

        ______TS("Typical case: Delete account request successful");
        accountRequest = sqlTestData.accountRequests.get("unregisteredInstructor1");
        searchContent = accountRequest.getEmail();
        searchPage.clearSearchBox();
        searchPage.inputSearchContent(searchContent);
        searchPage.clickSearchButton();
        searchPage.clickDeleteAccountRequestButton(accountRequest);
        assertNull(BACKDOOR.getAccountRequest(accountRequest.getEmail(), accountRequest.getInstitute()));
    }

    private String getExpectedStudentDetails(Student student) {
        return String.format("%s [%s] (%s)", student.getCourse().getId(),
                student.getSection() == null
                ? Const.DEFAULT_SECTION
                : student.getSection().getName(), student.getTeam().getName());
    }

    private String getExpectedStudentHomePageLink(Student student) {
        return student.isRegistered() ? createFrontendUrl(Const.WebPageURIs.STUDENT_HOME_PAGE)
                .withUserId(student.getGoogleId())
                .toAbsoluteString()
                : "";
    }

    private String getExpectedStudentManageAccountLink(Student student) {
        return student.isRegistered() ? createFrontendUrl(Const.WebPageURIs.ADMIN_ACCOUNTS_PAGE)
                .withParam(Const.ParamsNames.INSTRUCTOR_ID, student.getGoogleId())
                .toAbsoluteString()
                : "";
    }

    private int getExpectedNumExpandedRows(Student student) {
        int expectedNumExpandedRows = 2;
        for (FeedbackSession sessions : sqlTestData.feedbackSessions.values()) {
            if (sessions.getCourse().equals(student.getCourse())) {
                expectedNumExpandedRows += 1;
                if (sessions.getResultsVisibleFromTime().isBefore(Instant.now())) {
                    expectedNumExpandedRows += 1;
                }
            }
        }
        return expectedNumExpandedRows;
    }

    private String getExpectedInstructorHomePageLink(Instructor instructor) {
        String googleId = instructor.isRegistered() ? instructor.getGoogleId() : "";
        return createFrontendUrl(Const.WebPageURIs.INSTRUCTOR_HOME_PAGE)
                .withUserId(googleId)
                .toAbsoluteString();
    }

    private String getExpectedInstructorManageAccountLink(Instructor instructor) {
        String googleId = instructor.isRegistered() ? instructor.getGoogleId() : "";
        return createFrontendUrl(Const.WebPageURIs.ADMIN_ACCOUNTS_PAGE)
                .withParam(Const.ParamsNames.INSTRUCTOR_ID, googleId)
                .toAbsoluteString();
    }

    @AfterClass
    public void classTeardown() {
        removeSqlDataBundle(sqlTestData);
    }
}
