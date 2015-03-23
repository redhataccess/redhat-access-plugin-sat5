package com.redhat.telemetry.integration.sat5;

public class BranchInfo {

  private String branchId;
  private String leafId;

  public BranchInfo() {
  }

  public BranchInfo(String branchId, String leafId) {
    this.branchId = branchId;
    this.leafId = leafId;
  }

  public void setLeafId(String leafId) {
    this.leafId = leafId;
  }

  public String getLeafId() {
    return this.leafId;
  }

  public void setBranchId(String branchId) {
    this.branchId = branchId;
  }

  public String getBranchId() {
    return this.branchId;
  }
}
