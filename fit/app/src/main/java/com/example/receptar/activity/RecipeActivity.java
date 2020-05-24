package com.example.receptar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receptar.R;
import com.example.receptar.adapter.CommentAdapter;
import com.example.receptar.java.LoginData;
import com.example.receptar.java.Recipe;
import com.example.receptar.viewmodel.RecipeViewModel;

import static com.example.receptar.adapter.RecipeAdapter.EXTRA_RECIPE_ID;

public class RecipeActivity extends BasicActivity<RecipeViewModel> {

    public static final int REQUEST_EDIT_RECIPE = 1;

    public static final String EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE";
    private int recipeId;

    private EditText commentBody;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, 0);

        viewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);
        updateContent();

        RecyclerView recyclerView = findViewById(R.id.comments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final CommentAdapter adapter = new CommentAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setComments(viewModel.getRecipeComments(recipeId));
        commentBody = findViewById(R.id.edit_text_comment);
        Button submitButton = findViewById(R.id.button_publish_comment);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (commentBody.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Komentár musí obsahovať text!", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.addComment(commentBody.getText().toString(), recipeId);
                adapter.setComments(viewModel.getRecipeComments(recipeId));
                Toast.makeText(getApplicationContext(), "Komentár uverejnený!", Toast.LENGTH_SHORT).show();
                commentBody.setText("");
            }
        });
    }

    private void updateContent() {
        setTitle(viewModel.getRecipeHeader(recipeId));

        ((TextView) findViewById(R.id.text_view_recipe_title)).setText(viewModel.getRecipeTitle(recipeId));
        ((TextView) findViewById(R.id.text_view_recipe_author)).setText(viewModel.getRecipeAuthor(recipeId));

        TextView recipeTextView = findViewById(R.id.text_view_recipe_text);
        recipeTextView.setText(viewModel.getRecipeText(recipeId));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.recipe_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit_recipe) {
            editRecipe();
            return true;
        } else if (item.getItemId() == R.id.delete_recipe) {
            deleteRecipe();
            finish();
            Toast.makeText(getApplicationContext(), "Recept odstránený!", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void editRecipe() {
        Intent intent = new Intent(getApplicationContext(), EditRecipeActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipeId);
        intent.putExtra(EXTRA_REQUEST_CODE, REQUEST_EDIT_RECIPE);
        startActivityForResult(intent, REQUEST_EDIT_RECIPE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_EDIT_RECIPE && resultCode == RESULT_OK) {
            String title = intent.getStringExtra(EditRecipeActivity.EXTRA_TITLE);
            String steps = intent.getStringExtra(EditRecipeActivity.EXTRA_STEPS);
            int recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, 0);
            Recipe updatedRecipe = new Recipe(LoginData.getLoggedUserId(), title, steps);
            updatedRecipe.setId(recipeId);
            viewModel.update(updatedRecipe);
            Toast.makeText(getApplicationContext(), "Recept uložený!", Toast.LENGTH_SHORT).show();
            updateContent();
        }
    }

    public void deleteRecipe() {
        viewModel.deleteById(recipeId);
    }

}
