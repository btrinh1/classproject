/*
 * Copyright 2013 Adam Saturna
 *  Copyright 2013 Brian Trinh
 *  Copyright 2013 Ethan Mykytiuk
 *  Copyright 2013 Prateek Srivastava (@f2prateek)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.cmput301.recipebot.model;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

import static com.cmput301.recipebot.util.LogUtils.makeLogTag;

/**
 * A controller class that makes it transparent when switching between network and local calls.
 * All UI components should interact with this.
 */
public class PantryModel {

    private static final String LOGTAG = makeLogTag(PantryModel.class);

    private DatabaseHelper dbHelper;
    private static PantryModel instance;
    // A cache of last know ingredients.
    private ArrayList<Ingredient> mPantry;
    private ArrayList<PantryView> views;

    private PantryModel(Context context) {
        dbHelper = new DatabaseHelper(context);
        mPantry = new ArrayList<Ingredient>();
        views = new ArrayList<PantryView>();
    }

    public interface PantryView {
        public void update(ArrayList<Ingredient> ingredientList);
    }

    public void addView(PantryView view) {
        views.add(view);
    }

    public void deleteView(PantryView view) {
        views.remove(view);
    }

    public void notifyViews() {
        for (PantryView view : views) {
            view.update(mPantry);
        }
    }

    /**
     * Get the instance. Attaches to application context regardless of context.
     *
     * @param context
     * @return
     */
    public static PantryModel getInstance(Context context) {
        if (instance == null) {
            instance = new PantryModel(context.getApplicationContext());
        }
        return instance;
    }

    public ArrayList<Ingredient> loadPantry() {
        // Start an AsyncTask to load all items.
        new LoadPantryTask().execute();
        // In the meantime, return the cached version.
        return mPantry;
    }

    public void insertPantryItem(final Ingredient ingredient) {
        new InsertPantryItemTask().execute(ingredient);
    }

    public void deletePantryItem(String id) {
        new DeletePantryItemTask().execute(id);
    }

    private class InsertPantryItemTask extends AsyncTask<Ingredient, Void, ArrayList<Ingredient>> {

        @Override
        protected ArrayList<Ingredient> doInBackground(Ingredient... params) {
            Ingredient ingredient = params[0];
            dbHelper.insertPantryItem(ingredient);
            return dbHelper.getAllPantryItems();
        }

        @Override
        protected void onPostExecute(ArrayList<Ingredient> ingredients) {
            mPantry = ingredients;
            notifyViews();
            super.onPostExecute(ingredients);
        }
    }

    private class DeletePantryItemTask extends AsyncTask<String, Void, ArrayList<Ingredient>> {

        @Override
        protected ArrayList<Ingredient> doInBackground(String... params) {
            String id = params[0];
            dbHelper.deletePantryItem(id);
            return dbHelper.getAllPantryItems();
        }

        @Override
        protected void onPostExecute(ArrayList<Ingredient> ingredients) {
            mPantry = ingredients;
            notifyViews();
            super.onPostExecute(ingredients);
        }
    }

    private class LoadPantryTask extends AsyncTask<Void, Void, ArrayList<Ingredient>> {

        @Override
        protected ArrayList<Ingredient> doInBackground(Void... params) {
            return dbHelper.getAllPantryItems();
        }

        @Override
        protected void onPostExecute(ArrayList<Ingredient> ingredients) {
            mPantry = ingredients;
            notifyViews();
            super.onPostExecute(ingredients);
        }
    }

}
