package com.redhat.cloud.custompolicies.engine;

import com.redhat.cloud.custompolicies.engine.model.SystemProfile;
import java.io.FileInputStream;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * @author hrupp
 */
public class SPReader {


  public static void main(String[] args) throws Exception {

    SPReader spr = new SPReader();
    spr.run();
  }

  private void run() {
    Jsonb jsonb = JsonbBuilder.create();
    try (FileInputStream fis = new FileInputStream("system_profile_sample.json")) {
      SystemProfile sp = jsonb.fromJson(fis, SystemProfile.class);
//      Object sp = jsonb.fromJson(fis, Object.class);
      System.out.println(sp);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
