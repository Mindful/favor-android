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

package com.favor.library;

/**
 * Created by josh on 10/29/14.
 */
public class Worker {
    private static native void _createContact(String address, int type, String displayName, boolean addressExists) throws FavorException;
    public static void createContact(String address, Core.MessageType type, String displayName) throws FavorException{
        _createContact(address, Core.intFromType(type), displayName, false);
    }
    public static void createContact(String address, Core.MessageType type, String displayName, boolean addressExists) throws FavorException{
        _createContact(address, Core.intFromType(type), displayName, addressExists);
    }
}
