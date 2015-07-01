package com.redhat.telemetry.integration.sat5.json;

public class BranchInfo {

  private String remote_branch;
  private int remote_leaf;
  private Product product;
  private String hostname;

  public BranchInfo() {
  }

  public BranchInfo(String remote_branch, int remote_leaf, Product product, String hostname) {
    this.remote_branch = remote_branch;
    this.remote_leaf = remote_leaf;
    this.product = product;
    this.hostname = hostname;
  }

  public void setRemote_leaf(int remote_leaf) {
    this.remote_leaf = remote_leaf;
  }

  public int getRemote_leaf() {
    return this.remote_leaf;
  }

  public void setRemote_branch(String remote_branch) {
    this.remote_branch = remote_branch;
  }

  public String getRemote_branch() {
    return this.remote_branch;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Product getProduct() {
    return this.product;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getHostname() {
    return this.hostname;
  }
}
