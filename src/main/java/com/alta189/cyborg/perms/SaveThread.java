/*
 * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
 *
 * This file is part of CyborgPerms
 *
 * CyborgPerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CyborgPerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.alta189.cyborg.perms;

public class SaveThread extends Thread {
	@Override
	public void run() {
		while (!isInterrupted()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (CyborgUser user : PermissionManager.getUsers()) {
				user.flush();
				PermissionManager.getDatabase().save(CyborgUser.class, user);
			}

			for (CyborgGroup group : PermissionManager.getGroups()) {
				group.flush();
				PermissionManager.getDatabase().save(CyborgGroup.class, group);
			}
		}
	}
}
