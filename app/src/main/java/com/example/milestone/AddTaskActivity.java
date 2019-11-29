package com.example.milestone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateCourseMutation;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListCoursesQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateTaskInput;
import type.ModelCourseFilterInput;
import type.ModelStringFilterInput;

public class AddTaskActivity extends AppCompatActivity {

    private final String TAG = AddTaskActivity.class.getSimpleName();
    private String username;
    private ArrayList<ListCoursesQuery.Item> mCourses;
    private List<ListCoursesQuery.Item> mcData = new ArrayList<>();
    private Spinner taskCourseSpinner,prioritySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        username = UserDataController.getUsername();

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        query();
        addItemsToSpinner();
        setPrioritySpinner();

        Button btnAddTask = (Button) findViewById(R.id.addtaskbtn);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runMutation();
                setContentView(R.layout.activity_main);
            }
        });

    }
    public void runMutation(){
        final String courseName = taskCourseSpinner.getSelectedItem().toString();
        final String taskTitle = ((EditText) findViewById(R.id.tasktitlebox)).getText().toString();
        final String dueDate = getDueDate();
        final String priority = prioritySpinner.getSelectedItem().toString();
        final double percentage = Double.valueOf(getPercentage());
        final String comments = ((EditText) findViewById(R.id.taskcommentbox)).getText().toString();
        TaskController.addATask(courseName,taskTitle,dueDate,priority,comments,percentage,getCourseID());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AddTaskActivity.this, "Added task: " + taskTitle, Toast.LENGTH_SHORT).show();
                AddTaskActivity.this.finish();
                Log.i("Results", "Added Task");
            }
        });
    }
    /*
    REMOVED WHILE TESTING TASKCONTROLLER
    public void runMutation(){
        final String id = UUID.randomUUID().toString();
        final String courseName = taskCourseSpinner.getSelectedItem().toString();
        final String taskTitle = ((EditText) findViewById(R.id.tasktitlebox)).getText().toString();
        final String dueDate = getDueDate();
        final String priority = prioritySpinner.getSelectedItem().toString();
        final double percentage = Double.valueOf(getPercentage());
        final String comments = ((EditText) findViewById(R.id.taskcommentbox)).getText().toString();
        final boolean completed = false;
        CreateTaskInput createTaskInput = CreateTaskInput.builder().
                id(id).
                author(UserDataController.getUsername()).
                coursename(courseName).
                title(taskTitle).
                duedate(dueDate).
                priority(priority).
                percentage(percentage).
                comments(comments).
                completed(completed).
                taskCourseId(getCourseID()).build();

        CreateTaskMutation addTaskMutation = CreateTaskMutation.builder()
                .input(createTaskInput)
                .build();

        ClientFactory.appSyncClient().mutate(addTaskMutation).enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<CreateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreateTaskMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddTaskActivity.this, "Added task", Toast.LENGTH_SHORT).show();
                    AddTaskActivity.this.finish();
                    Log.i("Results", "Added Task");
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddTaskMutation", e);
                    Toast.makeText(AddTaskActivity.this, "Failed to add Task", Toast.LENGTH_SHORT).show();
                    AddTaskActivity.this.finish();
                }
            });
        }
    };
    */
    public String getCourseID(){
        String cNameSelected = taskCourseSpinner.getSelectedItem().toString();
        int courseIndex = taskCourseSpinner.getSelectedItemPosition();
        String courseID = mcData.get(courseIndex).id();
        String cNameFromQuery = mcData.get(courseIndex).coursename();

        if(cNameSelected.equals(cNameFromQuery)){
            return courseID;
        }
        else{
            return "";
        }

    }

    public void setPrioritySpinner(){
        prioritySpinner = (Spinner) findViewById(R.id.taskpriority);

        List<String> priorityList = new ArrayList<>();
        priorityList.add("None");
        priorityList.add("Low");
        priorityList.add("Medium");
        priorityList.add("High");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, priorityList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(dataAdapter);
    }

    public String getDueDate(){
        DatePicker dpDate = (DatePicker) findViewById(R.id.datePicker);
        StringBuilder sb = new StringBuilder();
        sb.append((dpDate.getMonth()+1)+"/"+dpDate.getDayOfMonth()+"/"+dpDate.getYear());

        return sb.toString();
    }

    public String getPercentage(){
        Button tskbtn = (Button) findViewById(R.id.addtaskbtn);
        String percentage = ((EditText) findViewById(R.id.taskpercentagebox)).getText().toString();
        double percent = Double.valueOf(percentage);
        if(percent >= 0 || percent <= 100){
            tskbtn.setEnabled(true);
            return String.valueOf(percent);
        }
        else{
            tskbtn.setEnabled(false);
            return "Invalid Percentage";
        }
    }

    public void query(){
        ModelStringFilterInput msfi = ModelStringFilterInput.builder().eq(UserDataController.getUsername()).build();
        ModelCourseFilterInput mcfi = ModelCourseFilterInput.builder().author(msfi).build();
        ClientFactory.appSyncClient().query(ListCoursesQuery.builder().filter(mcfi).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListCoursesQuery.Data> queryCallback = new GraphQLCall.Callback<ListCoursesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCoursesQuery.Data> response) {
            mCourses = new ArrayList<>(response.data().listCourses().items());

            runOnUiThread(new Runnable(){
                public void run(){
                    setCourses(mCourses);
                }
            });


            Log.i(TAG, "Retrieved Courses: " + mCourses.toString() + mCourses.size());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());

        }
    };

    public void setCourses(List<ListCoursesQuery.Item> items){
        mcData = items;
        addItemsToSpinner();
    }

    public void addItemsToSpinner() {
        taskCourseSpinner = (Spinner) findViewById(R.id.taskCourseSpinner);
        List<String> courseList = new ArrayList<>();

        for(int i = 0; i<mcData.size();i++){
            courseList.add(mcData.get(i).coursename());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskCourseSpinner.setAdapter(dataAdapter);

    }
}
