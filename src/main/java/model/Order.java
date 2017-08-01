package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Order defines a model for the order entity
 *
 * Created by: Nilkanth Patel
 */
public class Order implements Serializable {
  private String id;

  private Collection<String> items;

  public Order(){
    this.items = new ArrayList<String>();
  }

  public Order(String id, Collection<String> items) {
    super();
    this.id = id;
    this.items = items;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }


  public Collection<String> getItems() {
    return items;
  }

  public void setItems(Collection<String> items) {
    this.items = items;
  }

  /**
   * @param item
   */
  public void addItem(String item) {
    this.items.add(item);
  }

  @Override
  public String toString() {
    return "Order [id=" + id + " Items = " + this.items + "]";
  }

}


