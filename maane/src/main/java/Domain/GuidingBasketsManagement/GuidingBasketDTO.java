package Domain.GuidingBasketsManagement;

import java.util.List;

public class GuidingBasketDTO {
    String basketID;
    String title;
    String description;
    List<String> labels;
    // TODO: how would we save the file?

    public GuidingBasketDTO(String basketID ,String title, String description, List<String> labels) {
        this.basketID = basketID;
        this.title = title;
        this.description = description;
        this.labels = labels;
    }

    public GuidingBasketDTO(GuidingBasket basket){
        this.basketID = basket.getBasketID();
        this.title = basket.getTitle();
        this.description = basket.getDescription();
        this.labels = basket.getLabels();
    }

    public String getBasketID() {
        return basketID;
    }

    public void setBasketID(String basketID) {
        this.basketID = basketID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
