package anlin.softdev.kuleuven.memories;

import org.junit.Test;

import java.util.ArrayList;

import database.Label;
import database.LabelGroup;
import database.NewSQLService;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    /**
     * test if the url generator is correct or not (using char array to replace label name)
     */
    @Test
    public void testUrlGenerator()
    {
        Label label = new Label("xie");
        Label label1 = new Label("liu");
        Label label2 = new Label("lin");
        Label label3 = new Label("li");
        ArrayList<Label> labels = new ArrayList<>();
        labels.add(label);
        labels.add(label1);
        labels.add(label2);
        labels.add(label3);

        LabelGroup labelGroup = new LabelGroup(labels);

        NewSQLService testService = new NewSQLService();
        String realUrl = "https://studev.groept.be/api/a18_sd602/findLabels/" +
                "xie/liu/lin/li/labelnamee";
        assertEquals(realUrl,testService.addLabelGroup(labelGroup));
    }
}