package com.example.myfirstapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myfirstapplication.data.AppDatabase;
import com.example.myfirstapplication.data.dao.NoteDao;
import com.example.myfirstapplication.data.dao.SubTaskDao;
import com.example.myfirstapplication.data.entities.Note;
import com.example.myfirstapplication.data.entities.SubTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private EditText editTextCategory;
    private LinearLayout notesContainer;
    private LinearLayout completedNotesContainer;
    private LinearLayout inputContainer;
    private LinearLayout categoriesContainer;
    private HorizontalScrollView categoriesScrollView;
    private TextView completedNotesLabel;
    private DatePicker datePicker;
    private TimePicker timePicker;

    private Button buttonMenu;
    private boolean isEditMode = false;

    private String currentCategory = "All";
    private View currentSwipedView = null;
    private Handler autoDeleteHandler = new Handler();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    private AppDatabase db;
    private NoteDao noteDao;
    private SubTaskDao subTaskDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(this);
        noteDao = db.noteDao();
        subTaskDao = db.subTaskDao();

        initViews();
        setupListeners();
        loadCategories();
        displayNotes(currentCategory);
        scheduleOldNotesDeletion();
    }

    private void initViews() {
        editText = findViewById(R.id.editTextInput);
        editTextCategory = findViewById(R.id.editTextCategory);
        notesContainer = findViewById(R.id.notesContainer);
        completedNotesContainer = findViewById(R.id.completedNotesContainer);
        inputContainer = findViewById(R.id.inputContainer);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        categoriesScrollView = findViewById(R.id.categoriesScrollView);
        completedNotesLabel = findViewById(R.id.completedNotesLabel);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);

        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), null);
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));

        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);

        DatePicker datePicker = findViewById(R.id.datePicker);
        if (datePicker != null) {
            datePicker.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            try {
                datePicker.setCalendarViewShown(true);
                datePicker.setSpinnersShown(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TimePicker timePicker = findViewById(R.id.timePicker);
        if (timePicker != null) {
            timePicker.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        }
        buttonMenu = findViewById(R.id.buttonMenu);
    }

    private void setupListeners() {
        Button buttonAddNote = findViewById(R.id.buttonAddNote);
        Button buttonSave = findViewById(R.id.buttonSave);

        buttonAddNote.setOnClickListener(v -> {
            inputContainer.setVisibility(View.VISIBLE);
            editText.requestFocus();
            showKeyboard(editText);
        });

        buttonSave.setOnClickListener(v -> saveNote());
        buttonMenu.setOnClickListener(v -> toggleEditMode());

    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        loadCategories();
    }
    private void loadCategories() {
        noteDao.getAllCategories().observe(this, categories -> {
            categoriesContainer.removeAllViews();
            addCategoryButton("All");
            for (String category : categories) {
                addCategoryButton(category);
            }
        });
    }

    private void addCategoryButton(String categoryName) {
        LinearLayout categoryContainer = new LinearLayout(this);
        categoryContainer.setOrientation(LinearLayout.HORIZONTAL);
        categoryContainer.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, 0, 8, 0);
        categoryContainer.setLayoutParams(containerParams);

        Button categoryButton = new Button(this);
        categoryButton.setText(categoryName);
        categoryButton.setAllCaps(false);
        categoryButton.setPadding(16, 8, 16, 8);
        categoryButton.setGravity(Gravity.CENTER);

        GradientDrawable buttonBackground = new GradientDrawable();
        buttonBackground.setShape(GradientDrawable.RECTANGLE);
        buttonBackground.setCornerRadius(dpToPx(16));
        buttonBackground.setColor(currentCategory.equals(categoryName) ?
                ContextCompat.getColor(this, R.color.purple_500) :
                ContextCompat.getColor(this, R.color.gray));
        categoryButton.setBackground(buttonBackground);
        categoryButton.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        categoryButton.setLayoutParams(buttonParams);

        categoryButton.setOnClickListener(v -> {
            if (!isEditMode) {
                currentCategory = categoryName;
                displayNotes(categoryName);
                loadCategories();
            }
        });

        if (isEditMode && !categoryName.equals("All")) {
            Button optionsButton = new Button(this);
            optionsButton.setText("⋯");
            optionsButton.setAllCaps(false);
            optionsButton.setPadding(8, 8, 8, 8);
            optionsButton.setGravity(Gravity.CENTER);
            optionsButton.setTextSize(16);
            optionsButton.setTextColor(Color.DKGRAY);
            optionsButton.setBackground(null);

            LinearLayout.LayoutParams optionsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            optionsParams.setMargins(4, 0, 0, 0);
            optionsButton.setLayoutParams(optionsParams);

            Button deleteCategoryButton = new Button(this);
            deleteCategoryButton.setText("✕");
            deleteCategoryButton.setAllCaps(false);
            deleteCategoryButton.setPadding(8, 8, 8, 8);
            deleteCategoryButton.setGravity(Gravity.CENTER);
            deleteCategoryButton.setTextSize(14);
            deleteCategoryButton.setTextColor(Color.WHITE);

            GradientDrawable deleteBg = new GradientDrawable();
            deleteBg.setShape(GradientDrawable.RECTANGLE);
            deleteBg.setCornerRadius(dpToPx(8));
            deleteBg.setColor(Color.RED);
            deleteCategoryButton.setBackground(deleteBg);

            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            deleteParams.setMargins(4, 0, 0, 0);
            deleteCategoryButton.setLayoutParams(deleteParams);
            deleteCategoryButton.setVisibility(View.GONE);

            optionsButton.setOnClickListener(v -> {
                if (deleteCategoryButton.getVisibility() == View.VISIBLE) {
                    deleteCategoryButton.setVisibility(View.GONE);
                } else {
                    deleteCategoryButton.setVisibility(View.VISIBLE);
                }
            });

            deleteCategoryButton.setOnClickListener(v -> {
                deleteCategory(categoryName);
            });

            categoryContainer.addView(categoryButton);
            categoryContainer.addView(optionsButton);
            categoryContainer.addView(deleteCategoryButton);
        } else {
            categoryContainer.addView(categoryButton);
        }

        categoriesContainer.addView(categoryContainer);
        categoriesScrollView.setVisibility(View.VISIBLE);
    }

    private void deleteCategory(String categoryName) {
        new Thread(() -> {
            noteDao.deleteNotesByCategory(categoryName);

            runOnUiThread(() -> {
                Toast.makeText(this, "Category '" + categoryName + "' deleted", Toast.LENGTH_SHORT).show();
                if (currentCategory.equals(categoryName)) {
                    currentCategory = "All";
                }
                loadCategories();
                displayNotes(currentCategory);
            });
        }).start();
    }

    private void saveNote() {
        String noteText = editText.getText().toString().trim();
        if (noteText.isEmpty()) {
            Toast.makeText(this, "Please enter note text", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = editTextCategory.getText().toString().trim();
        if (category.isEmpty()) {
            category = "All";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                timePicker.getHour(), timePicker.getMinute());
        long deadlineMillis = calendar.getTimeInMillis();

        String finalCategory = category;
        new Thread(() -> {
            Note note = new Note(noteText, finalCategory, deadlineMillis);
            long noteId = noteDao.insert(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                resetInputFields();
                displayNotes(currentCategory);
                if (!finalCategory.equals("All")) {
                    loadCategories();
                }
            });
        }).start();
    }

    private void displayNotes(String category) {
        String displayCategory = category;

        noteDao.getActiveNotesByCategory(displayCategory).observe(this, notes -> {
            notesContainer.removeAllViews();
            for (Note note : notes) {
                createNoteView(note, notesContainer, false);
            }
        });

        noteDao.getCompletedNotesByCategory(displayCategory).observe(this, notes -> {
            completedNotesContainer.removeAllViews();
            if (notes != null && !notes.isEmpty()) {
                completedNotesLabel.setVisibility(View.VISIBLE);
                completedNotesContainer.setVisibility(View.VISIBLE);
                for (Note note : notes) {
                    createNoteView(note, completedNotesContainer, true);
                }
            } else {
                completedNotesLabel.setVisibility(View.GONE);
                completedNotesContainer.setVisibility(View.GONE);
            }
        });
    }

    private void createNoteView(Note note, LinearLayout container, boolean isCompleted) {
        RelativeLayout noteContainer = new RelativeLayout(this);
        noteContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        noteContainer.setPadding(0, 8, 0, 8);
        noteContainer.setBackgroundColor(Color.WHITE);

        Button deleteButton = new Button(this);
        deleteButton.setId(View.generateViewId());
        GradientDrawable deleteBackground = new GradientDrawable();
        deleteBackground.setShape(GradientDrawable.RECTANGLE);
        deleteBackground.setCornerRadius(dpToPx(8));
        deleteBackground.setColor(Color.RED);
        deleteButton.setBackground(deleteBackground);
        deleteButton.setText("DELETE");
        deleteButton.setTextColor(Color.WHITE);
        deleteButton.setTextSize(12);

        RelativeLayout.LayoutParams deleteButtonParams = new RelativeLayout.LayoutParams(
                dpToPx(80),
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        deleteButtonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        deleteButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        deleteButton.setLayoutParams(deleteButtonParams);
        deleteButton.setVisibility(View.GONE);

        deleteButton.setOnClickListener(v -> deleteNote(note));

        LinearLayout noteContent = new LinearLayout(this);
        noteContent.setId(View.generateViewId());
        noteContent.setOrientation(LinearLayout.VERTICAL);
        noteContent.setBackgroundResource(R.drawable.note_background);
        noteContent.setBackgroundColor(Color.WHITE);

        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        contentParams.addRule(RelativeLayout.START_OF, deleteButton.getId());
        noteContent.setLayoutParams(contentParams);

        LinearLayout taskLayout = new LinearLayout(this);
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);
        taskLayout.setGravity(Gravity.CENTER_VERTICAL);
        taskLayout.setPadding(16, 16, 16, 16);
        taskLayout.setBackgroundColor(Color.WHITE);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(isCompleted);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleNoteCompletion(note, isChecked);
        });

        TextView noteView = new TextView(this);
        noteView.setText(note.text);
        noteView.setTextSize(16);
        noteView.setPadding(16, 0, 16, 0);
        noteView.setTextColor(Color.BLACK);
        if (isCompleted) {
            noteView.setPaintFlags(noteView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            noteView.setTextColor(Color.GRAY);
        }

        Button addSubTaskButton = new Button(this);
        addSubTaskButton.setText("+");
        addSubTaskButton.setAllCaps(false);
        GradientDrawable addBackground = new GradientDrawable();
        addBackground.setShape(GradientDrawable.RECTANGLE);
        addBackground.setCornerRadius(dpToPx(8));
        addBackground.setColor(ContextCompat.getColor(this, R.color.purple_500));
        addSubTaskButton.setBackground(addBackground);
        addSubTaskButton.setTextColor(Color.WHITE);
        addSubTaskButton.setOnClickListener(v -> showAddSubTaskDialog(note));

        taskLayout.addView(checkBox);
        taskLayout.addView(noteView);
        taskLayout.addView(addSubTaskButton);
        noteContent.addView(taskLayout);

        new Thread(() -> {
            List<SubTask> subTasks = subTaskDao.getSubTasksForNote(note.id);
            runOnUiThread(() -> {
                if (!subTasks.isEmpty()) {
                    LinearLayout subTasksLayout = new LinearLayout(MainActivity.this);
                    subTasksLayout.setOrientation(LinearLayout.VERTICAL);
                    subTasksLayout.setPadding(48, 0, 16, 16);
                    subTasksLayout.setBackgroundColor(Color.WHITE);

                    for (SubTask subTask : subTasks) {
                        LinearLayout subTaskLayout = new LinearLayout(MainActivity.this);
                        subTaskLayout.setOrientation(LinearLayout.HORIZONTAL);
                        subTaskLayout.setGravity(Gravity.CENTER_VERTICAL);
                        subTaskLayout.setPadding(0, 8, 0, 0);
                        subTaskLayout.setBackgroundColor(Color.WHITE);

                        CheckBox subTaskCheckBox = new CheckBox(MainActivity.this);
                        subTaskCheckBox.setChecked(subTask.isCompleted);
                        subTaskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            updateSubTaskStatus(subTask, isChecked);
                        });

                        TextView subTaskView = new TextView(MainActivity.this);
                        subTaskView.setText(subTask.text);
                        subTaskView.setTextSize(14);
                        subTaskView.setPadding(16, 0, 0, 0);
                        subTaskView.setTextColor(Color.BLACK);
                        if (subTask.isCompleted) {
                            subTaskView.setPaintFlags(subTaskView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            subTaskView.setTextColor(Color.GRAY);
                        }

                        subTaskLayout.addView(subTaskCheckBox);
                        subTaskLayout.addView(subTaskView);
                        subTasksLayout.addView(subTaskLayout);
                    }

                    noteContent.addView(subTasksLayout);
                }

                TextView deadlineView = new TextView(MainActivity.this);
                deadlineView.setText(dateFormat.format(new Date(note.deadline)));
                deadlineView.setTextSize(12);
                deadlineView.setPadding(48, 0, 16, 16);
                deadlineView.setTextColor(note.deadline < System.currentTimeMillis() ? Color.RED : Color.DKGRAY);
                noteContent.addView(deadlineView);

                setupSwipeHandler(noteContent, deleteButton, noteContainer);

                noteContainer.addView(deleteButton);
                noteContainer.addView(noteContent);
                container.addView(noteContainer);
            });
        }).start();
    }

    private void setupSwipeHandler(View swipeView, Button deleteButton, RelativeLayout container) {
        swipeView.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;
            private boolean isSwiping = false;
            private final int SWIPE_THRESHOLD = dpToPx(10);
            private final int SWIPE_DISTANCE_THRESHOLD = dpToPx(50);

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        isSwiping = false;
                        if (currentSwipedView != null && currentSwipedView != container) {
                            hideDeleteButton((RelativeLayout) currentSwipedView);
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float x = event.getX();
                        float y = event.getY();
                        float deltaX = x - startX;
                        float deltaY = y - startY;

                        if (Math.abs(deltaX) > SWIPE_THRESHOLD && Math.abs(deltaX) > Math.abs(deltaY)) {
                            isSwiping = true;
                            if (deltaX < 0 && swipeView.getTranslationX() >= -deleteButton.getWidth()) {
                                swipeView.setTranslationX(deltaX);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isSwiping) {
                            float delta = event.getX() - startX;
                            if (delta < -SWIPE_DISTANCE_THRESHOLD) {
                                showDeleteButton(container);
                                currentSwipedView = container;
                            } else {
                                hideDeleteButton(container);
                            }
                            return true;
                        }
                        return false;
                }
                return false;
            }
        });
    }

    private void showDeleteButton(RelativeLayout container) {
        Button deleteButton = container.findViewById(container.getChildAt(0).getId());
        View noteContent = container.findViewById(container.getChildAt(1).getId());

        deleteButton.setVisibility(View.VISIBLE);
        noteContent.animate()
                .translationX(-deleteButton.getWidth())
                .setDuration(200)
                .start();
    }

    private void hideDeleteButton(RelativeLayout container) {
        Button deleteButton = container.findViewById(container.getChildAt(0).getId());
        View noteContent = container.findViewById(container.getChildAt(1).getId());

        noteContent.animate()
                .translationX(0)
                .setDuration(200)
                .withEndAction(() -> deleteButton.setVisibility(View.GONE))
                .start();
    }

    private void deleteNote(Note note) {
        new Thread(() -> {
            noteDao.delete(note);
            runOnUiThread(() -> {
                displayNotes(currentCategory);
                if (currentSwipedView != null) {
                    hideDeleteButton((RelativeLayout) currentSwipedView);
                    currentSwipedView = null;
                }
            });
        }).start();
    }

    private void toggleNoteCompletion(Note note, boolean isCompleted) {
        new Thread(() -> {
            note.isCompleted = isCompleted;
            note.completionTime = isCompleted ? System.currentTimeMillis() : 0;
            noteDao.update(note);

            if (isCompleted) {
                List<SubTask> subTasks = subTaskDao.getSubTasksForNote(note.id);
                for (SubTask subTask : subTasks) {
                    subTask.isCompleted = true;
                    subTaskDao.updateSubTaskStatus(subTask.id, true);
                }
            }

            runOnUiThread(() -> displayNotes(currentCategory));
        }).start();
    }

    private void showAddSubTaskDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add SubTask for: " + note.text);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String subTaskText = input.getText().toString().trim();
            if (!subTaskText.isEmpty()) {
                addSubTask(note, subTaskText);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addSubTask(Note note, String subTaskText) {
        new Thread(() -> {
            SubTask subTask = new SubTask(note.id, subTaskText);
            subTaskDao.insert(subTask);
            runOnUiThread(() -> displayNotes(currentCategory));
        }).start();
    }

    private void updateSubTaskStatus(SubTask subTask, boolean isCompleted) {
        new Thread(() -> {
            subTask.isCompleted = isCompleted;
            subTaskDao.updateSubTaskStatus(subTask.id, isCompleted);

            List<SubTask> subTasks = subTaskDao.getSubTasksForNote(subTask.noteId);
            boolean allCompleted = true;
            for (SubTask st : subTasks) {
                if (!st.isCompleted) {
                    allCompleted = false;
                    break;
                }
            }

            if (allCompleted && !subTasks.isEmpty()) {
                Note note = noteDao.getNoteWithSubTasks(subTask.noteId).getValue().note;
                if (note != null && !note.isCompleted) {
                    note.isCompleted = true;
                    note.completionTime = System.currentTimeMillis();
                    noteDao.update(note);
                }
            }
        }).start();
    }

    private void scheduleOldNotesDeletion() {
        autoDeleteHandler.postDelayed(() -> {
            new Thread(() -> {
                noteDao.deleteOldCompletedNotes(System.currentTimeMillis());
                autoDeleteHandler.postDelayed(this::scheduleOldNotesDeletion, 3600000);
            }).start();
        }, 3600000);
    }

    private void resetInputFields() {
        editText.setText("");
        editTextCategory.setText("");
        inputContainer.setVisibility(View.GONE);
        hideKeyboard(editText);

        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), null);
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoDeleteHandler.removeCallbacksAndMessages(null);
        if (db != null) {
            db.close();
        }
    }
}