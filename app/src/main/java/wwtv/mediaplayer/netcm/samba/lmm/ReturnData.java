package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

/**
 * Data list first return to item examples class.
 */
public class ReturnData {

    // Path or other identification
    private String id;

    // current page
    private int page;

    // total page num
    private int total;

    // focal position
    private int position;

    // 0 means list and 1 means grid
    private int viewMode;

    private String diractoryName;

    public ReturnData(String id, int page, int position) {
        this.id = id;
        this.page = page;
        this.position = position;
    }

    public ReturnData(String id, int page, int total, int position) {
        this.id = id;
        this.page = page;
        this.total = total;
        this.position = position;
    }

    public ReturnData(int viewMode,String id, int page, int position) {
        this.viewMode = viewMode;
        this.id = id;
        this.page = page;
        this.position = position;
    }
    public ReturnData(int viewMode,String id, int page, int position ,String diractoryName) {
        this.viewMode = viewMode;
        this.id = id;
        this.page = page;
        this.position = position;
        this.diractoryName = diractoryName;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }
     /**
     * @return the viewMode
     */
    public int getViewMode() {
        return viewMode;
    }

      /**
      * @return the diractoryName
      */
     public String getDiractoryName() {
         return diractoryName;
     }

}
