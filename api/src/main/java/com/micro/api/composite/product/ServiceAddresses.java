package com.micro.api.composite.product;

public class ServiceAddresses {
  private final String cmp;
  private final String pro;
  private final String rev;
  private final String rec;

  public ServiceAddresses() {
    this.cmp = null;
    this.pro = null;
    this.rev = null;
    this.rec = null;
  }

  public ServiceAddresses(
      String compositeAddress,
      String productAddress,
      String reviewAddress,
      String recommendationAddress) {

    this.cmp = compositeAddress;
    this.pro = productAddress;
    this.rev = reviewAddress;
    this.rec = recommendationAddress;
  }

  public String getCmp() {
    return cmp;
  }

  public String getPro() {
    return pro;
  }

  public String getRev() {
    return rev;
  }

  public String getRec() {
    return rec;
  }
}
