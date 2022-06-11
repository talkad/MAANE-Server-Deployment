package UnitTesting.GuidingBaskets;

import Domain.CommonClasses.Response;
import Domain.GuidingBasketsManagement.GuidingBasket;
import Domain.GuidingBasketsManagement.GuidingBasketDTO;
import Domain.GuidingBasketsManagement.SearchEngine;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SearchEngineTest {

    @Before
    public void setUp() throws Exception {
        // first add
        GuidingBasket basket = new GuidingBasket("0", "General", "Kenobi", new String[]{"Hello", "There"});
        SearchEngine.getInstance().addBasket(new GuidingBasketDTO(basket), "0");
        // second add
        basket = new GuidingBasket("1", "Anakin", "Skywalker", new String[]{"I", "Hate", "Sand"});
        SearchEngine.getInstance().addBasket(new GuidingBasketDTO(basket), "1");
        // third add
        basket = new GuidingBasket("2", "Padme", "Amidala", new String[]{"Big", "Sad"});
        SearchEngine.getInstance().addBasket(new GuidingBasketDTO(basket), "2");
        // adding to remove later
        basket = new GuidingBasket("3", "Darth", "Sidious", new String[]{"Am", "Senate"});
        SearchEngine.getInstance().addBasket(new GuidingBasketDTO(basket), "3");
        // adding to modify later
        basket = new GuidingBasket("4", "Luke", "Skywalker", new String[]{"New", "Hope"});
        SearchEngine.getInstance().addBasket(new GuidingBasketDTO(basket), "4");
        // adding to modify labels later
        basket = new GuidingBasket("5", "Han", "Solo", new String[]{"Blaster", "Falcon"});
        SearchEngine.getInstance().addBasket(new GuidingBasketDTO(basket), "5");
    }

    @Test
    public void search() {
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search("", new String[]{"Hello", "Sad"});
        assertFalse(result.isFailure());
        List<GuidingBasketDTO> result_list = result.getResult();
        assertEquals(2, result_list.size()); // checking we got the right amount of results

        //going over the results
        for(GuidingBasketDTO dto: result_list){
            assertTrue(dto.getTitle().equals("General") || dto.getTitle().equals("Padme"));
        }
    }

    @Test
    public void searchNotExists() {
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search("", new String[]{"You"});
        assertTrue(result.isFailure()); // should fail
    }

    @Test
    public void searchNull() {
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search(null, null);
        assertTrue(result.isFailure()); // should fail
    }

    @Test
    public void addBasket() {
        GuidingBasket basket = new GuidingBasket("10", "General", "Grievous", new String[]{"Bold", "One"});
        Response<Boolean> response = SearchEngine.getInstance().addBasket(new GuidingBasketDTO(basket), "10");
        assertFalse(response.isFailure()); // should work

        // looking it up
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search("", new String[]{"Bold"});
        assertFalse(result.isFailure());
        List<GuidingBasketDTO> result_list = result.getResult();
        assertEquals(result_list.size(), 1); // checking we got the right amount of results

        assertEquals(result_list.get(0).getTitle(), "General");
    }

    @Test
    public void addBasketNull() {
        // null id
        GuidingBasket basket = new GuidingBasket("11", "Mace", "Windo", new String[]{"Purple", "Lightsaber"});
        Response<Boolean> response = SearchEngine.getInstance().addBasket(new GuidingBasketDTO(basket), null);
        assertTrue(response.isFailure()); // should fail

        // null object
        response = SearchEngine.getInstance().addBasket(null, "42");
        assertTrue(response.isFailure()); // should fail
    }

    @Test
    public void removeBasket() {
        GuidingBasket basket = new GuidingBasket("3", "Darth", "Sidious", new String[]{"Am", "Senate"});
        Response<Boolean> response = SearchEngine.getInstance().removeBasket(new GuidingBasketDTO(basket));
        assertFalse(response.isFailure()); // should work

        // looking it up. shouldn't find it
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search("", new String[]{"Am"});
        assertTrue(result.isFailure()); // should fail
    }

    @Test
    public void removeBasketNull() {
        Response<Boolean> response = SearchEngine.getInstance().removeBasket(null);
        assertTrue(response.isFailure()); // should fail

        GuidingBasket basket = new GuidingBasket(null, "Darth", "Sidious", new String[]{"Am", "Senate"});
        response = SearchEngine.getInstance().removeBasket(new GuidingBasketDTO(basket));
        assertTrue(response.isFailure()); // should fail
    }

    @Test
    public void setBasketTitle() {
        GuidingBasket basket = new GuidingBasket("4", "Luke", "Skywalker", new String[]{"New", "Hope"});
        Response<Boolean> response = SearchEngine.getInstance().setBasketTitle(new GuidingBasketDTO(basket), "Princess");
        assertFalse(response.isFailure()); // should work

        // looking up the change
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search("", new String[]{"Hope"});
        assertFalse(result.isFailure());
        List<GuidingBasketDTO> result_list = result.getResult();
        assertEquals(result_list.size(), 1); // checking we got the right amount of results

        assertEquals(result_list.get(0).getTitle(), "Princess");
    }

    @Test
    public void setBasketTitleNotExisting() {
        // what we care about is the id
        GuidingBasket basket = new GuidingBasket("5000", "Luke", "Skywalker", new String[]{"New", "Hope"});
        Response<Boolean> response = SearchEngine.getInstance().setBasketTitle(new GuidingBasketDTO(basket), "Princess");
        assertTrue(response.isFailure()); // should fail
    }

    @Test
    public void setBasketTitleNull() {
        // new title null
        GuidingBasket basket = new GuidingBasket("5000", "Luke", "Skywalker", new String[]{"New", "Hope"});
        Response<Boolean> response = SearchEngine.getInstance().setBasketTitle(new GuidingBasketDTO(basket), null);
        assertTrue(response.isFailure()); // should fail
    }

    @Test
    public void setBasketDescription() {
        GuidingBasket basket = new GuidingBasket("4", "Luke", "Skywalker", new String[]{"New", "Hope"});
        Response<Boolean> response = SearchEngine.getInstance().setBasketDescription(new GuidingBasketDTO(basket), "Leia");
        assertFalse(response.isFailure()); // should work

        // looking up the change
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search("", new String[]{"Hope"});
        assertFalse(result.isFailure());
        List<GuidingBasketDTO> result_list = result.getResult();
        assertEquals(result_list.size(), 1); // checking we got the right amount of results

        assertEquals(result_list.get(0).getDescription(), "Leia");
    }

    @Test
    public void setBasketDescriptionNotExisting() {
        // what we care about is the id
        GuidingBasket basket = new GuidingBasket("5000", "Luke", "Skywalker", new String[]{"New", "Hope"});
        Response<Boolean> response = SearchEngine.getInstance().setBasketDescription(new GuidingBasketDTO(basket), "Leia");
        assertTrue(response.isFailure()); // should fail
    }

    @Test
    public void setBasketDescriptionNull() {
        // new description null
        GuidingBasket basket = new GuidingBasket("5000", "Luke", "Skywalker", new String[]{"New", "Hope"});
        Response<Boolean> response = SearchEngine.getInstance().setBasketDescription(new GuidingBasketDTO(basket), null);
        assertTrue(response.isFailure()); // should fail
    }

    @Test
    public void addRemoveLabelAdding() {
        GuidingBasket basket = new GuidingBasket("5", "Han", "Solo", new String[]{"Blaster", "Falcon"});
        // adding
        Response<Boolean> response = SearchEngine.getInstance().addRemoveLabel(new GuidingBasketDTO(basket), "Chewbacca", 0);
        assertFalse(response.isFailure()); // should work

        // looking up the addition
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search("", new String[]{"Chewbacca"});
        assertFalse(result.isFailure());
        List<GuidingBasketDTO> result_list = result.getResult();
        assertEquals(result_list.size(), 1); // checking we got the right amount of results

        assertEquals(result_list.get(0).getTitle(), "Han");
    }

    @Test
    public void addRemoveLabelRemoving() {
        GuidingBasket basket = new GuidingBasket("5", "Han", "Solo", new String[]{"Blaster", "Falcon"});
        // removing
        Response<Boolean> response = SearchEngine.getInstance().addRemoveLabel(new GuidingBasketDTO(basket), "Falcon", 1);
        assertFalse(response.isFailure()); // should work

        // looking up the addition
        Response<List<GuidingBasketDTO>> result = SearchEngine.getInstance().search("", new String[]{"Falcon"});
        assertTrue(result.isFailure()); // should fail
    }

    @Test
    public void addRemoveLabelNotExisting() {
        // we care about the id
        GuidingBasket basket = new GuidingBasket("6000", "Han", "Solo", new String[]{"Blaster", "Falcon"});
        // removing
        Response<Boolean> response = SearchEngine.getInstance().addRemoveLabel(new GuidingBasketDTO(basket), "Falcon", 1);
        assertTrue(response.isFailure()); // should fail
    }

    @Test
    public void addRemoveLabelNullLabel() {
        // we care about the id
        GuidingBasket basket = new GuidingBasket("5", "Han", "Solo", new String[]{"Blaster", "Falcon"});
        // removing
        Response<Boolean> response = SearchEngine.getInstance().addRemoveLabel(new GuidingBasketDTO(basket), null, 1);
        assertTrue(response.isFailure()); // should fail
    }

    @Test
    public void addRemoveLabelInvalidAction() {
        // we care about the id
        GuidingBasket basket = new GuidingBasket("5", "Han", "Solo", new String[]{"Blaster", "Falcon"});
        // removing
        Response<Boolean> response = SearchEngine.getInstance().addRemoveLabel(new GuidingBasketDTO(basket), null, 100);
        assertTrue(response.isFailure()); // should fail
    }
}