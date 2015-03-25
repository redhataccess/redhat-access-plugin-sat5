package com.redhat.telemetry.integration.sat5;

public class BranchInfo {

  private String remote_branch;
  private int remote_leaf;

  public BranchInfo() {
  }

  public BranchInfo(String remote_branch, int remote_leaf) {
    this.remote_branch = remote_branch;
    this.remote_leaf = remote_leaf;
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
}
