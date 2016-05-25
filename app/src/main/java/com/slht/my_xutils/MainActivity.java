package com.slht.my_xutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.config.DbConfigs;
import org.xutils.db.DbManagerImpl;
import org.xutils.db.Selector;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    DbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbManager = x.getDb(((MyApplication) getApplication()).getDaoConfig());

    }


    public void deleteClick(View view) {
        try {
            dbManager.delete(User.class, WhereBuilder.b("id", "=", 5));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void updateClick(View view) {
        try {
            User user = new User("888888@136.com", "张三feng");
//            user.setId(3);
//            dbManager.update(user,"email","name");
            KeyValue keyValue1 = new KeyValue("email", user.getEmail());
            KeyValue keyValue2 = new KeyValue("name", user.getName());
            dbManager.update(User.class, WhereBuilder.b("id", "=", 2), keyValue1, keyValue2);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void findOneClick(View view) {
        User user = new User();
        user.setId(1);
        try {
            Toast.makeText(this, "dbManager.findById(User.class,user.getId()):" + dbManager.findById(User.class, user
                    .getId()), Toast.LENGTH_SHORT).show();
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void saveClick(View view) {
        User user = new User("xiaohong@qq.com", "xiaohong");
        try {
            dbManager.save(user);
            Toast.makeText(this, "success save", Toast.LENGTH_SHORT).show();
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void findAllClick(View view) {
        try {
//            List<User> users = dbManager.selector(User.class).findAll();
            List<User> users = dbManager.findAll(User.class);
            for (User u : users) {
                System.out.println(u.toString());
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
