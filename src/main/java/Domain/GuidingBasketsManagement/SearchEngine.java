package Domain.GuidingBasketsManagement;

import Domain.CommonClasses.Response;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class SearchEngine {

    private static SearchEngine instance;
    private List<GuidingBasket> baskets;

    private SearchEngine(){
        baskets = new LinkedList<>();
    }

    public static SearchEngine getInstance(){
        if(instance == null){
            instance = new SearchEngine();
        }

        return instance;
    }

    // TODO: for now we ignore the query
    // TODO: also for now we ignore "sub office" id

    /**
     * returns the best matching guiding baskets to the query filtered by the given labels
     * @param query the query to search with
     * @param labels the labels to filter upon
     * @return best matching guiding baskets according to predetermined metrics
     */
    public Response<List<GuidingBasketDTO>> search(String query, String[] labels){
        List<GuidingBasketDTO> toReturn = new LinkedList<>();

        if(labels != null && query != null){
            for(GuidingBasket basket: baskets){
                for(String label: labels){
                    if (basket.getLabels().contains(label)){
                        toReturn.add(new GuidingBasketDTO(basket));
                        break;
                    }
                }
            }
        }

        if (toReturn.size() == 0){
            return new Response<>(null, true, "No basket matches the search");
        }
        else{
            return new Response<>(toReturn, false, null);
        }
    }

    /**
     * checks if the given id already exists in the system
     * @param id the id to check
     * @return true if exists, false otheriwse
     */
    private boolean idExists(String id){
        for (GuidingBasket basket: baskets){
            if(basket.getBasketID().equals(id)){
                return true;
            }
        }

        return false;
    }

    /**
     * adds a new guiding basket to the system
     * @param dto the guiding basket to add
     * @param newID the id of the new basket to add
     * @return response with the result of the operation
     */
    public Response<Boolean> addBasket(GuidingBasketDTO dto, String newID){
        if(dto != null && newID != null && !idExists(newID)){
            dto.setBasketID(newID);
            instance.baskets.add(new GuidingBasket(dto));
            return new Response<>(true, false, null);
        }

        return new Response<>(false, true, "Couldn't add the basket");
    }

    /**
     * removes a guiding basket from the system
     * @param dto the guiding basket to remove
     * @return response with the result of the operation
     */
    public Response<Boolean> removeBasket(GuidingBasketDTO dto){
        if (dto != null && dto.getBasketID() != null){
            if(instance.baskets.removeIf(b -> b.getBasketID().equals(dto.getBasketID()))){
                return new Response<>(true, false, null);
            }
        }

        return new Response<>(false, true, "Couldn't find basket to remove");
    }

    /**
     * gets a GuidingBasket based on its DTO version
     * @param dto the dto of the guiding basket asked
     * @return a GuidingBasket representation of the given GuidingBasketDTO
     */
    private GuidingBasket getBasket(GuidingBasketDTO dto){
        try{
           return baskets.stream().filter(b -> b.getBasketID().equals(dto.getBasketID()))
                    .findFirst().get();
        }
        catch (NoSuchElementException e){
            return null;
        }
    }

    /**
     * sets the title of a given basket
     * @param dto the guiding basket to update
     * @param newTitle the value of the new title
     * @return response with the result of the operation
     */
    public Response<Boolean> setBasketTitle(GuidingBasketDTO dto, String newTitle){

        if(newTitle != null){
            GuidingBasket selected = getBasket(dto);
            if(selected != null){
                selected.setTitle(newTitle);
                return new Response<>(true, false, null);
            }
            return new Response<>(false, true, "The selected basket does not exist");
        }

        return new Response<>(false, true, "A new title was not provided");
    }

    /**
     * sets the description of a given basket
     * @param dto the guiding basket to update
     * @param newDescription the value of the new description
     * @return response with the result of the operation
     */
    public Response<Boolean> setBasketDescription(GuidingBasketDTO dto, String newDescription){
        if(newDescription != null){
            GuidingBasket selected = getBasket(dto);
            if(selected != null){
                selected.setDescription(newDescription);
                return new Response<>(true, false, null);
            }
            return new Response<>(false, true, "The selected basket does not exist");
        }

        return new Response<>(false, true, "A new description was not provided");
    }

    /**
     * adds or removes a label from a given guiding basket based on the param of the operation
     * @param dto the guiding basket to modify
     * @param label the label to add or remove
     * @param action 1 or 0. 0 - add label, 1 - remove label
     * @return response with the result of the operation
     */
    // action = 0 - add
    // action = 1 - remove
    public Response<Boolean> addRemoveLabel(GuidingBasketDTO dto, String label, int action){
        if(label != null){
            GuidingBasket selected = getBasket(dto);
            if(selected != null){
                if(action == 0){
                    return selected.addLabel(label);
                }
                else if (action == 1){
                    return selected.removeLabel(label);
                }
                else{
                    return new Response<>(false, true, "Invalid action");
                }
            }
            return new Response<>(false, true, "The selected basket does not exist");
        }

        return new Response<>(false, true, "The label was not provided");
    }
}
