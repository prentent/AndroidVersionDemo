package com.example.androidversiondemo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.example.androidversiondemo.utils.LogUtils;

import java.util.List;

/*
获取任务栈信息
 */
public class MyAppTask {
    public void getAppTasks(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager systemService = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTasks = systemService.getAppTasks();
            for (ActivityManager.AppTask appTask : appTasks) {
                ActivityManager.RecentTaskInfo taskInfo = appTask.getTaskInfo();//获取与此任务关联的RecentTaskInfo。
                int id = taskInfo.id; //如果该任务当前正在运行，这是它的标识符。如果它不运行，这将是-1。
                int affiliatedTaskId = taskInfo.affiliatedTaskId;//此任务的真实标识符，即使不运行也有效。
                ComponentName baseActivity = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    baseActivity = taskInfo.baseActivity;//作为任务中的第一个活动启动的组件。这可以看作是该任务的“应用程序”。
                }
                LogUtils.e(this, id + "---" + affiliatedTaskId + "---" + (baseActivity == null ? "" : baseActivity.getClassName()));
                //appTask.finishAndRemoveTask(); //完成此任务中的所有活动，并将其从最近的任务列表中删除。
                // appTask.moveToFront();//将此任务带到前台。如果它包含活动，它们将被带到前台，并在需要时重新创建它们的实例。如果它不包含活动，则将重新启动任务的根活动。
                //appTask.setExcludeFromRecents();//修改Intent#FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS此AppTask的根Intent中的标志。
                //appTask.startActivity();//在此任务中启动活动。将任务带到前台。如果此任务当前未处于活动状态（即其id <0），则将启动给定Intent的新活动作为任务的根，并将任务带到前台。否则，如果此任务当前处于活动状态且Intent未指定要在新任务中启动的活动，则将在任务之上启动给定Intent的新活动，并将任务带到前台。如果此任务当前处于活动状态且Intent指定Intent#FLAG_ACTIVITY_NEW_TASK 或将以其他方式启动到新任务，则活动未启动但此任务将被带到前台，并且如果合适，则将新意图传递给顶级活动。
                //换句话说，你通常要在这里使用一个Intent没有指定 Intent#FLAG_ACTIVITY_NEW_TASK或Intent#FLAG_ACTIVITY_NEW_DOCUMENT，并让系统做正确的事情。
            }
        }

    }
}
