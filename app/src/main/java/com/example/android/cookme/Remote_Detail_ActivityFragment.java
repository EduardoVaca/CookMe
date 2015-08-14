package com.example.android.cookme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.cookme.data.Ingredient;
import com.example.android.cookme.data.Recipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * A placeholder fragment containing a simple view.
 */
public class Remote_Detail_ActivityFragment extends Fragment {

    //private TextView mRecipeName;
    private TextView mInstructions;
    private ImageView mRecipePhoto;
    private ListView mIngredientList;
    private ArrayList<Ingredient> ingredients;
    private Button addButton;
    private Recipe mRecipe;
    private Bitmap decodedByte;


    public Remote_Detail_ActivityFragment() {
    }
/*

    The Details of a Remote Recipe once a User has clicked on the recipe from the list.

 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_remote__detail_, container, false);

        //mRecipeName = (TextView) rootView.findViewById(R.id.detail_recipe_name);
        mInstructions = (TextView) rootView.findViewById(R.id.remote_instructions);
        mRecipePhoto = (ImageView) rootView.findViewById(R.id.remote_recipe_picture_imageview);
        mIngredientList = (ListView) rootView.findViewById(R.id.ingredients_list_remote);
        addButton = (Button) rootView.findViewById(R.id.add_recipe_to_local_button);

        Intent intent = getActivity().getIntent();

        if(intent!= null && intent.hasExtra(Intent.EXTRA_TEXT)){

            mRecipe = (Recipe) intent.getSerializableExtra(Intent.EXTRA_TEXT);

            //mRecipeName.setText(mRecipe.getName());

            mInstructions.setText(mRecipe.getInstructions());
            LinkedList<Ingredient> listIngredients = mRecipe.getIngredients();

            RemoteIngredientAdapter remoteIngredientAdapter = new RemoteIngredientAdapter(getActivity(), R.id.ingredients_list_remote, listIngredients);

            ingredients = new ArrayList<>(listIngredients);

            mIngredientList.setAdapter(remoteIngredientAdapter);
            setListViewHeightBasedOnItems(mIngredientList);

            String imageStringReference = mRecipe.getImage();
            File filePath = new File(imageStringReference);

            int length = (int) filePath.length();
            byte [] bytes = new byte[length];

            FileInputStream input = null;
            try {
                input = new FileInputStream(filePath);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                input.read(bytes);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                input.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            String imageString = new String(bytes);

            Log.i("Image in Adapter", imageString);
            byte [] decodedString = Base64.decode(imageString, Base64.URL_SAFE);
            final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            mRecipePhoto.setImageBitmap(decodedByte);

            mRecipePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    String mPhotoPath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                            decodedByte, null, null);
                    if (mPhotoPath != null) {
                        intent.setDataAndType(Uri.parse(mPhotoPath), "image/*");
                        startActivity(intent);
                    } else {
                        Context context = getActivity();
                        CharSequence text = "No image to display!";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }


                }
            });

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String actualPath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                                                        decodedByte, null, null);

                    Utility.insertWholeRecipeInDb(getActivity(), mRecipe.getName(), mRecipe.getInstructions(),
                            actualPath, Utility.getBytes(decodedByte), ingredients);
                    Context context = getActivity();
                    CharSequence text = mRecipe.getName() + " recipe added to my recipes!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            });
        }

        return rootView;
    }

   


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.save_from_remote:
                String actualPath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                        decodedByte, null, null);

                Utility.insertWholeRecipeInDb(getActivity(), mRecipe.getName(), mRecipe.getInstructions(),
                        actualPath, Utility.getBytes(decodedByte), ingredients);
                Context context = getActivity();
                CharSequence text = mRecipe.getName() + " recipe added to my recipes!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * @param listView to be resized
     * @return true if the listView is successfully resized, false otherwise
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }


}
