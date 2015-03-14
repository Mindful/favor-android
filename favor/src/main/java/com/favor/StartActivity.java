/*
 * Copyright (C) 2015  Joshua Tanner (mindful.jt@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.favor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.favor.library.*;


public class StartActivity extends FavorActivity {

    //Must be declared in this activity to play nice with the XMl activity declaration
    public void beginClick(View view){
        Intent intent = new Intent(this, CoreActivity.class);
        startActivity(intent);
        //Core.buildDefaultTextManager(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Button startButton = (Button) findViewById(R.id.begin);
        //startButton.setOnClickListener();
        Core.initialize(getApplicationContext());

    }

    @Override
    public void messageRefreshResponse(){
    }

    @Override
    public void addressRefreshResponse() {
    }

}
