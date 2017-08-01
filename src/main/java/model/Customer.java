package model;

import java.io.Serializable;


/**
 * Customer defines a model for the customer entity
 *
 * Created by: Nilkanth Patel
 */
public class Customer implements Serializable {
  String firstName;
  String lastName;

  String address;

  public Customer() {}

  public Customer(String fname, String lName, String addressLine) {
    this.firstName = fname;
    this.lastName = lName;
    this.address = addressLine;
  }

  @Override
  public String toString()
  {
    return "Customer { Name=" + this.firstName + " " + this.lastName + " address=" + this.address + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (!(o instanceof Customer))
      return false;

    Customer cust = (Customer) o;
    return (cust.firstName.equals(firstName) && cust.lastName.equals(lastName) && cust.address.equals(address));
  }

  /**
   * improved hashcode implementation
   * @return hashcode
   */
  /*@Override
  public int hashCode() {
    return this.firstName.hashCode() + this.lastName.hashCode() + this.address.hashCode();
  }*/

  /**
   * weak hashcode implementation
   * @return hashcode
   */
  @Override
  public int hashCode() {
    return this.lastName.hashCode();
  }
}
