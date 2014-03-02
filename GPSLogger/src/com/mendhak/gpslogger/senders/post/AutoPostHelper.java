package com.mendhak.gpslogger.senders.post;

import com.mendhak.gpslogger.common.IActionListener;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.senders.IFileSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/26/14
 * Time: 9:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoPostHelper implements IActionListener, IFileSender {

   IActionListener callback;

   public AutoPostHelper(IActionListener callback)
   {
      this.callback = callback;
   }

   @Override
   public void UploadFile(List<File> files)
   {
      Utilities.LogInfo("AutoPostHelper - UploadFile");

      // TODO
      // This sender no longer deals with files
      // Replace interface as appropriate for sending db records
//      ArrayList<File> filesToSend = new ArrayList<File>();

//      //If a zip file exists, remove others
//      for (File f : files)
//      {
//         filesToSend.add(f);
//
//         if (f.getName().contains(".zip"))
//         {
//            filesToSend.clear();
//            filesToSend.add(f);
//            break;
//         }
//      }


//      Thread t = new Thread(new AutoSendHandler(filesToSend.toArray(new File[filesToSend.size()]), this));
      Thread t = new Thread(new AutoSendHandler(this));
      t.start();
   }

   public void OnComplete()
   {
      // This was a success
      Utilities.LogInfo("POST complete");

      callback.OnComplete();
   }

   public void OnFailure()
   {
      callback.OnFailure();
   }

   @Override
   public boolean accept(File dir, String name)
   {
      return name.toLowerCase().endsWith(".zip")
              || name.toLowerCase().endsWith(".gpx")
              || name.toLowerCase().endsWith(".kml");
   }

}

class AutoSendHandler implements Runnable
{

   private final IActionListener helper;

   public AutoSendHandler(IActionListener helper)
   {
      this.helper = helper;
   }

   public void run()
   {
      try
      {
         // TODO

         Utilities.LogInfo("POSTing...");

         try {
            Thread.sleep(2000);
         } catch (InterruptedException e) {

         }

//         if (success)
//         {
//            helper.OnComplete();
//         }
//         else
//         {
//            helper.OnFailure();
//         }

         helper.OnComplete();
      }
      catch (Exception e)
      {
         helper.OnFailure();
         Utilities.LogError("AutoSendHandler.run", e);
      }

   }

}