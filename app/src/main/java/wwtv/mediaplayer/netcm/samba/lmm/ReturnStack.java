package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

import java.util.ArrayList;
import java.util.List;

/**
 * File browsing path stack.
 */
public class ReturnStack {

    private List<ReturnData> rdList = new ArrayList<ReturnData>();

    private int topPosition = -1;

    /**
     * Access storage capacity.
     * 
     * @return
     */
    public int getTankage() {
        return topPosition + 1;
    }

    /**
     * push stack.
     * 
     * @param parentId
     */
    public void push(ReturnData rd) {
        rdList.add(rd);
        topPosition++;
    }

    /**
     * pop.
     * 
     * @return Recent data into the stack.
     */
    public ReturnData pop() {
        if (topPosition < 0) {
            return null;
        }

        try {
            return rdList.get(topPosition);
        } finally {
            rdList.remove(topPosition);
            topPosition--;
        }
    }

    /**
     * Empty all pressure into the data in the stack.
     */
    public void clear() {
        while (topPosition >= 0) {
            pop();
        }
    }

    public int size() {
      return rdList.size();
    }
}
