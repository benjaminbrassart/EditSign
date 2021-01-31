package fr.bbrassart;

import fr.bbrassart.util.EditSignUtils;
import fr.bbrassart.util.EditSignUtils13;
import fr.bbrassart.util.EditSignUtils15;
import fr.bbrassart.util.EditSignUtils8;
import org.junit.Assert;
import org.junit.Test;

public class EditSignTest {

    @Test
    public void getMostSuitableUtils() {
        Assert.assertNull(EditSign.getMostSuitableUtils(6));

        EditSignUtils utils = EditSign.getMostSuitableUtils(8).get();

        Assert.assertTrue(utils instanceof EditSignUtils8);

        utils = EditSign.getMostSuitableUtils(10).get();

        Assert.assertTrue(utils instanceof EditSignUtils8);

        utils = EditSign.getMostSuitableUtils(13).get();

        Assert.assertTrue(utils instanceof EditSignUtils13);

        utils = EditSign.getMostSuitableUtils(16).get();

        Assert.assertTrue(utils instanceof EditSignUtils15);
    }
}
