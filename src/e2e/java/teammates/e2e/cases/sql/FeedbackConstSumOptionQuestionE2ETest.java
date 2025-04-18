package teammates.e2e.cases.sql;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import teammates.common.datatransfer.questions.FeedbackConstantSumQuestionDetails;
import teammates.common.datatransfer.questions.FeedbackConstantSumResponseDetails;
import teammates.common.util.Const;
import teammates.e2e.pageobjects.FeedbackSubmitPage;
import teammates.e2e.pageobjects.InstructorFeedbackEditPage;
import teammates.storage.sqlentity.FeedbackQuestion;
import teammates.storage.sqlentity.FeedbackResponse;

/**
 * SUT: {@link Const.WebPageURIs#INSTRUCTOR_SESSION_EDIT_PAGE}, {@link Const.WebPageURIs#SESSION_SUBMISSION_PAGE}
 *      specifically for ConstSumOption questions.
 */
public class FeedbackConstSumOptionQuestionE2ETest extends BaseFeedbackQuestionE2ETest {

    @Override
    protected void prepareTestData() {
        testData = doRemoveAndRestoreDataBundle(
                loadSqlDataBundle("/FeedbackConstSumOptionQuestionE2ETestSql.json"));

        instructor = testData.instructors.get("instructor");
        course = testData.courses.get("course");
        feedbackSession = testData.feedbackSessions.get("openSession");
        student = testData.students.get("alice.tmms@FCSumOptQn.CS2104");
    }

    @Test
    @Override
    public void testAll() {
        testEditPage();
        logout();
        testSubmitPage();
    }

    @Override
    protected void testEditPage() {
        InstructorFeedbackEditPage feedbackEditPage = loginToFeedbackEditPage();

        ______TS("verify loaded question");
        FeedbackQuestion loadedQuestion = testData.feedbackQuestions.get("qn1ForFirstSession");
        FeedbackConstantSumQuestionDetails questionDetails =
                (FeedbackConstantSumQuestionDetails) loadedQuestion.getQuestionDetailsCopy();
        feedbackEditPage.verifyConstSumQuestionDetails(1, questionDetails);

        ______TS("add new question");
        // add new question exactly like loaded question
        loadedQuestion.setQuestionNumber(2);
        feedbackEditPage.addConstSumOptionQuestion(loadedQuestion);

        feedbackEditPage.verifyConstSumQuestionDetails(2, questionDetails);
        verifyPresentInDatabase(loadedQuestion);

        ______TS("copy question");
        FeedbackQuestion copiedQuestion = testData.feedbackQuestions.get("qn1ForSecondSession");
        questionDetails = (FeedbackConstantSumQuestionDetails) copiedQuestion.getQuestionDetailsCopy();
        feedbackEditPage.copyQuestion(copiedQuestion.getCourseId(),
                copiedQuestion.getQuestionDetailsCopy().getQuestionText());
        copiedQuestion.getFeedbackSession().setCourse(course);
        copiedQuestion.setQuestionNumber(3);
        copiedQuestion.setFeedbackSession(feedbackSession);

        feedbackEditPage.verifyConstSumQuestionDetails(3, questionDetails);
        verifyPresentInDatabase(copiedQuestion);

        ______TS("edit question");
        questionDetails = (FeedbackConstantSumQuestionDetails) testData.feedbackQuestions.get("qn1ForFirstSession").getQuestionDetailsCopy();
        List<String> options = questionDetails.getConstSumOptions();
        options.add("Edited option.");
        questionDetails.setConstSumOptions(options);
        questionDetails.setPointsPerOption(true);
        questionDetails.setPoints(1000);
        loadedQuestion.setQuestionDetails(questionDetails);
        feedbackEditPage.editConstSumQuestion(2, questionDetails);
        feedbackEditPage.waitForPageToLoad();

        feedbackEditPage.verifyConstSumQuestionDetails(2, questionDetails);
        verifyPresentInDatabase(loadedQuestion);
    }

    @Override
    protected void testSubmitPage() {
        FeedbackSubmitPage feedbackSubmitPage = loginToFeedbackSubmitPage();

        ______TS("verify loaded question");
        FeedbackQuestion question = testData.feedbackQuestions.get("qn1ForFirstSession");
        feedbackSubmitPage.verifyConstSumQuestion(1, "",
                (FeedbackConstantSumQuestionDetails) question.getQuestionDetailsCopy());

        ______TS("submit response");
        FeedbackResponse response = getResponse(question, Arrays.asList(50, 20, 30));
        feedbackSubmitPage.fillConstSumOptionResponse(1, "", response);
        feedbackSubmitPage.clickSubmitQuestionButton(1);
        
        verifyPresentInDatabase(response);

        ______TS("check previous response");
        feedbackSubmitPage = getFeedbackSubmitPage();
        feedbackSubmitPage.verifyConstSumOptionResponse(1, "", response);

        ______TS("edit response");
        response = getResponse(question, Arrays.asList(23, 47, 30));
        feedbackSubmitPage.fillConstSumOptionResponse(1, "", response);
        feedbackSubmitPage.clickSubmitQuestionButton(1);

        feedbackSubmitPage = getFeedbackSubmitPage();
        feedbackSubmitPage.verifyConstSumOptionResponse(1, "", response);
        verifyPresentInDatabase(response);
    }

    private FeedbackResponse getResponse(FeedbackQuestion question, List<Integer> answers) {
        FeedbackConstantSumResponseDetails details = new FeedbackConstantSumResponseDetails();
        details.setAnswers(answers);
        FeedbackResponse resp = FeedbackResponse.makeResponse(question, student.getEmail(), student.getSection(), student.getEmail(), student.getSection(), details);
        return resp;
    }
}
