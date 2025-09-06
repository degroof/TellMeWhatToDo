package com.stevedegroof.tellmewhattodo;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * SettingsActivity provides an interface for managing application settings,
 * including backing up and restoring task data.
 * <p>
 * This activity allows users to:
 * <ul>
 *     <li>Back up their current tasks to a JSON file in the device's downloads folder.</li>
 *     <li>Restore tasks from a previously created JSON backup file.</li>
 * </ul>
 * The activity handles file operations, user confirmations, and interacts with the
 * {@link Tasks} singleton to manage task data.
 * </p>
 */
public class SettingsActivity extends ParentActivity
{
    private static final String FILENAME = "TMWTD_tasks_%s.json";

    // UI components
    private Button backupButton, restoreButton;
    private ActivityResultLauncher<String[]> restoreFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);

        // Initialize UI components
        backupButton = findViewById(R.id.button_backup);
        restoreButton = findViewById(R.id.button_restore);


        // Set up action buttons
        backupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get tasks json and save to file in downloads folder
                String json = Tasks.getInstance().getTasksJson();
                saveFile(json);

            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                confirmRestore();
            }
        });

        // Set up file picker for restoring tasks
         restoreFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri ->
                {
                    if (uri != null)
                    {
                        readFileContent(uri);
                    } else
                    {
                        new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                                .setTitle(R.string.restore_failed)
                                .setMessage(R.string.restore_cancelled_or_failed).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });

    }

    /**
     * Reads the content of a JSON file, validates it, and if valid, prompts the user to confirm
     * restoring tasks from the backup.
     * <p>
     * The method first reads the content of the file specified by the URI. It then attempts to
     * validate the JSON content. If the JSON is valid, it displays a dialog showing the number of
     * tasks found and the descriptions of the first four tasks. The user can then choose to
     * continue with the restore or cancel.
     * <p>
     * If the restore is confirmed, the tasks are loaded from the JSON, saved, and the current task
     * ID is cleared. A success message is displayed.
     * <p>
     * If the JSON is invalid or an I/O error occurs while reading the file, an error dialog is
     * shown.
     *
     * @param uri The URI of the JSON file to read.
     */
    private void readFileContent(Uri uri)
    {
        String json = "";
        try
        {
            ContentResolver cr = getContentResolver();
            AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");            InputStream inputStream = cr.openInputStream(uri);
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String line="";
            StringBuilder fileContents = new StringBuilder();
            while (line != null)
            {
                line = br.readLine();
                if (line != null) fileContents.append(line).append("\n");
            }
            br.close();
            isr.close();
            inputStream.close();
            json = fileContents.toString();
            try
            {
                String descriptions = Util.validateJson(getApplicationContext(),json);
                String[] d=descriptions.split("\\|\\|");
                int count = d.length;
                descriptions = "";
                for(int i=0;i<count&&i<4;i++) descriptions += d[i]+"\n";
                if (descriptions.length()>200) descriptions = descriptions.substring(0,200);
                String finalJson = json;
                new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                        .setTitle(R.string.verify_backup)
                        .setMessage(String.format(getString(R.string.found_tasks_in_backup), count, descriptions))
                        .setPositiveButton(getResources().getString(R.string.cont), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Tasks.getInstance().setTasksFromJson(finalJson);
                                Tasks.getInstance().save(SettingsActivity.this);
                                Tasks.getInstance().setCurrentTaskId(null);
                                dialog.dismiss();
                                Toast.makeText(SettingsActivity.this,R.string.tasks_successfully_restored, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
            catch (Exception e)
            {
                new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                        .setTitle(R.string.restore_failed)
                        .setMessage(R.string.not_valid_backup_file).
                        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).show();
                return;
            }
        }
        catch (IOException e)
        {
        }
    }

    /**
     * Saves the provided JSON string to a file in the device's public Downloads directory.
     * The filename will be in the format "TMWTD_tasks_yyyyMMdd_HHmmss.json", where
     * yyyyMMdd_HHmmss is the current timestamp.
     * Displays a success dialog upon successful file save, or does nothing if an IOException occurs.
     *
     * @param json The JSON string to be saved to the file.
     */
    private void saveFile(String json)
    {
        try
        {
            Date date = new Date();
            String timestamp = new SimpleDateFormat(getString(R.string.timestamp_format)).format(date);
            String tsname = String.format(FILENAME, timestamp);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), tsname);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(json.getBytes());
            fos.close();
            new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                    .setTitle(R.string.success)
                    .setMessage(String.format(getString(R.string.tasks_successfully_backed_up),tsname)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }).show();
        }
        catch (IOException e)
        {
        }
    }


    /**
     * Displays a confirmation dialog before proceeding with the restore operation.
     * The dialog warns the user that restoring will overwrite all existing tasks.
     * If the user confirms, the {@link #restore()} method is called.
     * If the user cancels, the dialog is dismissed.
     */
    private void confirmRestore()
    {
        new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setTitle(R.string.confirm_restore)
                .setMessage(R.string.this_will_overwrite)
                .setPositiveButton(R.string.restore, (dialog, which) -> restore())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Launches the file picker to select a JSON file for restoring tasks.
     * The file picker is restricted to "application/json" MIME type.
     */
    private void restore() {
        restoreFileLauncher.launch(new String[]{"application/json"});
    }

    @Override
    protected void onResume()
    {
        super.onResume();

    }


}
