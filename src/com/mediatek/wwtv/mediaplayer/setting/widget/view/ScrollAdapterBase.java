/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.wwtv.mediaplayer.setting.widget.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;

/**
 * The adapter for ScrollAdapterView added more features controlling life cycle.
 */
public interface ScrollAdapterBase extends ListAdapter {

    /**
     * optional method to be implemented by {@link Adapter}. {@link #viewRemoved(View)} is called
     * when a view is removed from ScrollAdapterView, this gives Adapter a chance to do some clean
     * up work: for example release resources, cancel image downloading task. The implementation
     * should not destroy the view hierarchy because the view might be recycled and passed as
     * parameter in next {@link Adapter#getView(int, View, ViewGroup)}.
     */
    void viewRemoved(View view);

    /**
     * Creates a scrap view to measure and decide the size of ScrollAdapterView , the difference
     * from {@link #getView(int, View, ViewGroup)} is that it shouldn't load any runtime data for
     * efficiency.
     */
    View getScrapView(ViewGroup parent);

}
