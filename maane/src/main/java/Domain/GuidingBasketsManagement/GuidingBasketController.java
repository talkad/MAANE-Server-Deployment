//package Domain.GuidingBasketsManagement;
//
//import Domain.CommonClasses.Response;
//import Domain.UsersManagment.UserController;
//
//import java.util.List;
//
//public class GuidingBasketController {
//
//    private static GuidingBasketController instance;
//    private static long id = 0;
//
//    public static GuidingBasketController getInstance(){
//        if(instance == null){
//            instance = new GuidingBasketController();
//        }
//
//        return instance;
//    }
//
//    /**
//     * generates a new ids to use for new guiding baskets
//     * @return a unique id for a basket
//     */
//    private String generateID(){
//        return String.valueOf(id++);
//    }
//
//    /**
//     * returns results for a query with the given labels
//     * @param query the query
//     * @param labels labels to filter with
//     * @return top results for the query with the given labels
//     */
//    public Response<List<GuidingBasketDTO>> search(String query, String[] labels){
//        return SearchEngine.getInstance().search(query, labels);
//    }
//
//    /**
//     * adds a new basket to the system
//     * @param username the user who adds the basket
//     * @param dto the basket to add
//     * @return response with the result of the operation
//     */
//    public Response<Boolean> addBasket(String username, GuidingBasketDTO dto){
//        String basketID = instance.generateID();
//        Response<String> response = UserController.getInstance().createBasket(username, basketID);
//
//        if(response.isFailure())
//            return new Response<>(false, true, response.getErrMsg());
//
//        return SearchEngine.getInstance().addBasket(dto, basketID);
//    }
//
//    /**
//     * removes a new basket from the system
//     * @param username the user who removes the basket
//     * @param dto the basket to remove
//     * @return response with the result of the operation
//     */
//    public Response<Boolean> removeBasket(String username, GuidingBasketDTO dto){
//        Response<String> response = UserController.getInstance().removeBasket(username, dto.getBasketID());
//
//        if(response.isFailure())
//            return  new Response<>(false, true, response.getErrMsg());
//
//        return SearchEngine.getInstance().removeBasket(dto);
//    }
//
//    /**
//     * sets a new title to the selected basket
//     * @param username the user who initiated the change
//     * @param dto the guiding basket to update
//     * @param newTitle the new title to update to
//     * @return response with the result of the operation
//     */
//    public Response<Boolean> setBasketTitle(String username, GuidingBasketDTO dto, String newTitle){
//        Response<Boolean> response = UserController.getInstance().hasCreatedBasket(username, dto.getBasketID());
//
//        if(response.isFailure())
//            return  response;
//
//        return SearchEngine.getInstance().setBasketTitle(dto, newTitle);
//    }
//
//    /**
//     * sets a new description to the selected basket
//     * @param username the user who initiated the change
//     * @param dto the guiding basket to update
//     * @param newDescription the new description to update to
//     * @return response with the result of the operation
//     */
//    public Response<Boolean> setBasketDescription(String username, GuidingBasketDTO dto, String newDescription){
//        Response<Boolean> response = UserController.getInstance().hasCreatedBasket(username, dto.getBasketID());
//
//        if(response.isFailure())
//            return  response;
//
//        return SearchEngine.getInstance().setBasketDescription(dto, newDescription);
//    }
//
//    /**
//     * adds a new label to the selected basket
//     * @param username the user who initiated the addition
//     * @param dto the guiding basket to add to
//     * @param labelToAdd the new label to add
//     * @return response with the result of the operation
//     */
//    public Response<Boolean> addBasketLabel(String username, GuidingBasketDTO dto, String labelToAdd){
//        Response<Boolean> response = UserController.getInstance().hasCreatedBasket(username, dto.getBasketID());
//
//        if(response.isFailure())
//            return  response;
//
//        return SearchEngine.getInstance().addRemoveLabel(dto, labelToAdd, 0);
//    }
//
//    /**
//     * removes a label from the selected basket
//     * @param username the user who initiated the deletion
//     * @param dto the guiding basket to remove from
//     * @param labelToRemove the lavel to remove
//     * @return response with the result of the operation
//     */
//    public Response<Boolean> removeBasketLabel(String username, GuidingBasketDTO dto, String labelToRemove){
//        Response<Boolean> response = UserController.getInstance().hasCreatedBasket(username, dto.getBasketID());
//
//        if(response.isFailure())
//            return  response;
//
//        return SearchEngine.getInstance().addRemoveLabel(dto, labelToRemove, 1);
//    }
//
//}
