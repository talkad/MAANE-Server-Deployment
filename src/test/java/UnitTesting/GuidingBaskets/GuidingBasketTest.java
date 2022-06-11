package UnitTesting.GuidingBaskets;

import Domain.CommonClasses.Response;
import Domain.GuidingBasketsManagement.GuidingBasket;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GuidingBasketTest {

    GuidingBasket basket;

    @Before
    public void setUp() throws Exception {
        basket = new GuidingBasket("0", "Hello", "There", new String[]{"General", "Kenobi"});
    }

    @Test
    public void addLabel() {
        assertFalse(basket.getLabels().contains("Bold")); // checking if the label we're about to add is there
        Response<Boolean> result = basket.addLabel("Bold"); // adding
        assertTrue(result.getResult()); // should work
        assertTrue(basket.getLabels().contains("Bold")); // checking if the label is there
    }

    @Test
    public void addLabelNull() {
        Response<Boolean> result = basket.addLabel(null); // adding
        assertTrue(result.isFailure()); // should fail
    }

    @Test
    public void addLabelAlreadyExists() {
        assertTrue(basket.getLabels().contains("General")); // checking if the label we're about to add again is there
        Response<Boolean> result = basket.addLabel("General"); // adding
        assertTrue(result.isFailure()); // should fail
    }

    @Test
    public void removeLabel() {
        assertTrue(basket.getLabels().contains("Kenobi")); // checking if the label we're about to remove is there
        Response<Boolean> result = basket.removeLabel("Kenobi"); // removing
        assertTrue(result.getResult()); // should work
        assertFalse(basket.getLabels().contains("Kenobi")); // checking if the label is not there
    }

    @Test
    public void removeLabelNull() {
        Response<Boolean> result = basket.removeLabel(null); // removing
        assertTrue(result.isFailure()); // should fail
    }

    @Test
    public void addLabelNotExists() {
        assertFalse(basket.getLabels().contains("Anakin")); // checking if the label we're about to remove is not there
        Response<Boolean> result = basket.removeLabel("Anakin"); // adding
        assertTrue(result.isFailure()); // should fail
    }
}