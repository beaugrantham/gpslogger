package com.mendhak.gpslogger.senders.post;

import com.mendhak.gpslogger.common.IActionListener;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.senders.IPublisher;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/26/14
 * Time: 9:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoPostHelper implements IActionListener, IPublisher {

   IActionListener callback;

   public AutoPostHelper(IActionListener callback)
   {
      this.callback = callback;
   }

   @Override
   public void publish()
   {
      Utilities.LogInfo("AutoPostHelper - publish");

      // TODO
      // Query for un-published records, pass into new thread below

//      Thread t = new Thread(new AutoSendHandler(filesToSend.toArray(new File[filesToSend.size()]), this));
      Thread t = new Thread(new AutoSendHandler(this));
      t.start();
   }

   public void onComplete()
   {
      // This was a success
      Utilities.LogInfo("POST complete");

      callback.onComplete();
   }

   public void onFailure()
   {
      callback.onFailure();
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
//            helper.onComplete();
//         }
//         else
//         {
//            helper.onFailure();
//         }

         helper.onComplete();
      }
      catch (Exception e)
      {
         helper.onFailure();
         Utilities.LogError("AutoSendHandler.run", e);
      }

   }

}