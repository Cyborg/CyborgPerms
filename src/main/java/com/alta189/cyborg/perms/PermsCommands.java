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

import com.alta189.cyborg.api.command.CommandContext;
import com.alta189.cyborg.api.command.CommandResult;
import com.alta189.cyborg.api.command.CommandSource;
import com.alta189.cyborg.api.command.ReturnType;
import com.alta189.cyborg.api.command.annotation.Command;
import org.pircbotx.User;

import static com.alta189.cyborg.api.command.CommandResultUtil.get;
import static com.alta189.cyborg.perms.PermissionManager.addGroup;
import static com.alta189.cyborg.perms.PermissionManager.getGroup;
import static com.alta189.cyborg.perms.PermissionManager.getUser;
import static com.alta189.cyborg.perms.PermissionManager.hasPerm;
import static com.alta189.cyborg.perms.PermissionManager.registerUser;

public class PermsCommands {
	
	private static final String newLine = System.getProperty("line.separator");
	
	@Command(name = "register", desc = "Register with CyborgPerms")
	public CommandResult register(CommandSource source, CommandContext context) {
		switch (source.getSource()) {
			case TERMINALUSER:
				return new CommandResult().setBody("Can't register from the terminal!");
			case USER:
				if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
					return null;
				}
				User user = source.getUser();
				if (context.getArgs() == null || context.getArgs().length < 2) {
					return get(ReturnType.MESSAGE, "Correct usage is .register <name> <password>", source, context);
				}
				if (context.getLocationType() == null || context.getLocationType() != CommandContext.LocationType.PRIVATE_MESSAGE) {
					return get(ReturnType.MESSAGE, "You may only register via a private message!", source, context);
				}
				if (user.getHostmask() == null) {
					return get(ReturnType.MESSAGE, "Your hostmask is null. Make sure you are in a channel with me", source, context);
				}
				if (getUser(context.getArgs()[0]) != null) {
					return get(ReturnType.MESSAGE, "Someone with this nick has already registered", source, context);
				}
				registerUser(context.getArgs()[0], user.getLogin(), user.getHostmask(), context.getArgs()[1]);
				return get(ReturnType.MESSAGE, "registered!", source, context);
		}
		return null;
	}

	@Command(name = "authenticate", desc = "Authenticates you with CyborgPerms", aliases = {"auth"})
	public CommandResult authenticate(CommandSource source, CommandContext context) {
		switch (source.getSource()) {
			case TERMINALUSER:
				return new CommandResult().setBody("Can't authenticate from the terminal!");
			case USER:
				if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
					return null;
				}
				if (context.getArgs() == null || context.getArgs().length < 2) {
					return get(ReturnType.MESSAGE, "Correct usage is .authenticate <name> <password>", source, context);
				}
				if (context.getLocationType() == null || context.getLocationType() != CommandContext.LocationType.PRIVATE_MESSAGE) {
					return get(ReturnType.MESSAGE, "You may only authenticate via a private message!", source, context);
				}
				CyborgUser user = getUser(context.getArgs()[0]);
				if (user == null) {
					return get(ReturnType.MESSAGE, "User does not exist!", source, context);
				}
				if (user.authenticatePassword(context.getArgs()[1])) {
					user.addTempHostame(source.getUser().getLogin() + "@" + source.getUser().getHostmask());
					return get(ReturnType.MESSAGE, "Authenticated!", source, context);
				} else {
					return get(ReturnType.MESSAGE, "Incorrect password!", source, context);
				}
		}
		return null;
	}

	@Command(name = "addhostname", desc = "Adds a hostname to a CyborgPerms account", aliases = {"addhost", "addhostmask"})
	public CommandResult addHostname(CommandSource source, CommandContext context) {
		switch (source.getSource()) {
			case TERMINALUSER:
				return new CommandResult().setBody("Can't add a hostname from the terminal!");
			case USER:
				if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
					return null;
				}
				if (context.getArgs() == null || context.getArgs().length < 2) {
					return get(ReturnType.MESSAGE, "Correct usage is .addhostname <name> <password>", source, context);
				}
				if (context.getLocationType() == null || context.getLocationType() != CommandContext.LocationType.PRIVATE_MESSAGE) {
					return get(ReturnType.MESSAGE, "You may only register via a private message!", source, context);
				}
				CyborgUser user = getUser(context.getArgs()[0]);
				if (user == null) {
					return get(ReturnType.MESSAGE, "User does not exist!", source, context);
				}
				if (source.getUser().getHostmask() == null) {
					return get(ReturnType.MESSAGE, "Your hostmask is null. Make sure you are in a channel with me", source, context);
				}
				if (user.authenticatePassword(context.getArgs()[1])) {
					user.addHostname(source.getUser().getLogin() + "@" + source.getUser().getHostmask());
					return get(ReturnType.MESSAGE, "Added hostname!", source, context);
				} else {
					return get(ReturnType.MESSAGE, "Incorrect password!", source, context);
				}
		}
		return null;
	}

	@Command(name = "addperm", desc = "adds perm to a user")
	public CommandResult addPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.add")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.TERMINALUSER ? "." : "") + "addperm <name> <perms>...";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null) {
			return get(ReturnType.MESSAGE, "User does not exist!", source, context);
		}
		for (int i = 1; i <= context.getArgs().length - 1; i++) {
			String perm = (context.getArgs()[i]);
			if (perm.startsWith("-")) {
				user.addNegatedPerm(perm.substring(1));
			} else {
				user.addPerm(perm);
			}
		}
		return null;
	}

	@Command(name = "remperm", desc = "removes perm from a user", aliases = {"rmperm", "removeperm", "delperm", "deleteperm"})
	public CommandResult remerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.remove")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.TERMINALUSER ? "." : "") + "remperm <name> <perms>...";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null) {
			return get(ReturnType.MESSAGE, "User does not exist!", source, context);
		}
		for (int i = 1; i <= context.getArgs().length - 1; i++) {
			String perm = (context.getArgs()[i]);
			if (perm.startsWith("-")) {
				user.removeNegatedPerm(perm.substring(1));
			} else {
				user.removePerm(perm);
			}
		}
		return null;
	}

	@Command(name = "listperms", desc = "list perms of a group")
	public CommandResult listPerms(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.list")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 1) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "listperms <name> ";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null) {
			return get(ReturnType.MESSAGE, "User does not exist!", source, context);
		}
		StringBuilder builder = new StringBuilder();
		for (String perm : user.getPerms()) {
			builder.append(perm);
			builder.append(newLine);
		}
		
		return get(ReturnType.MESSAGE, builder.toString(), source, context);
	}

	@Command(name = "wildcardperm", desc = "Removes or gives the wildcard perm to a user")
	public CommandResult wildcardPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.wildcard")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "wildcardperm <name> true/false";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null) {
			return get(ReturnType.MESSAGE, "User does not exist!", source, context);
		}
		if (context.getArgs()[1].equalsIgnoreCase("true") || context.getArgs()[1].equalsIgnoreCase("t") || context.getArgs()[1].equalsIgnoreCase("1")) {
			user.setWildcardPerm(true);
			return get(ReturnType.MESSAGE, "Set WildcardPerm to true", source, context);
		} else if (context.getArgs()[1].equalsIgnoreCase("false") || context.getArgs()[1].equalsIgnoreCase("f") || context.getArgs()[1].equalsIgnoreCase("0")) {
			user.setWildcardPerm(false);
			return get(ReturnType.MESSAGE, "Set WildcardPerm to false", source, context);
		} else {
			return get(ReturnType.MESSAGE, "Invalid setting. Please use true or false.", source, context);
		}
	}

	@Command(name = "userinfo", desc = "Returns info on a user")
	public CommandResult userInfo(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.userinfo")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 1) {
			String body =  "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "userinfo <name>";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null) {
			return get(ReturnType.MESSAGE, "User '" + context.getArgs()[0] + "' does not exist!", source, context);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("name: ").append(user.getName()).append(newLine);
		builder.append("wildcard: ").append(user.hasWildcardPerm()).append(newLine);
		builder.append("hostnames: ");
		int i = 0;
		for (String hostname : user.getRawHostnames()) {
			i++;
			builder.append(hostname);
			if (i != user.getRawHostnames().size()) {
				builder.append(", ");
			}
		}
		builder.append(newLine);
		builder.append("groups: ");
		i = 0;
		for (String group : user.getGroups()) {
			i++;
			builder.append(group);
			if (i != user.getRawHostnames().size()) {
				builder.append(", ");
			}
		}
		
		return get(ReturnType.MESSAGE, builder.toString(), source, context);
	}

	@Command(name = "hasperm", desc = "Checks if a user has perm")
	public CommandResult hasPermCommand(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.hasperm")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "hasperm <name> <perm> [ignore wildcard true/false]";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null) {
			return get(ReturnType.MESSAGE, "User does not exist!", source, context);
		}
		boolean ignoreWildcard = false;
		if (context.getArgs().length >= 3) {
			if (context.getArgs()[2].equalsIgnoreCase("true") || context.getArgs()[1].equalsIgnoreCase("t") || context.getArgs()[1].equalsIgnoreCase("1")) {
				ignoreWildcard = true;
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append("User '").append(user.getName()).append("' ");
		builder.append(user.hasPerm(context.getArgs()[1], ignoreWildcard) ? "has " : "does not have ");
		builder.append("permission '").append(context.getArgs()[1]).append("'");
		
		return get(ReturnType.MESSAGE, builder.toString(), source, context);
	}

	@Command(name = "addgroup", desc = "Adds a user to a group")
	public CommandResult userAddGroup(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "addgroup.hasperm")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "hasperm <name> <group> ";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null) {
			return get(ReturnType.MESSAGE, "User does not exist!", source, context);
		}
		if (getGroup(context.getArgs()[1]) == null) {
			return get(ReturnType.MESSAGE, "Group does not exist!", source, context);
		}
		user.addGroup(context.getArgs()[1]);
		return get(ReturnType.MESSAGE, "Added user to group!", source, context);
	}

	@Command(name = "remgroup", desc = "Removes a user from a group")
	public CommandResult userRemGroup(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "addgroup.hasperm")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "hasperm <name> <group> ";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null) {
			return get(ReturnType.MESSAGE, "User does not exist!", source, context);
		}
		user.removeGroup(context.getArgs()[1]);
		return get(ReturnType.MESSAGE, "Removed user from group!", source, context);
	}

	//Group Commands

	@Command(name = "gadd", desc = "Adds a new group")
	public CommandResult addGroupCommand(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.gadd")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 1) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "gadd <name>";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group != null) {
			return get(ReturnType.MESSAGE, "Group already exists!", source, context);
		}
		group = new CyborgGroup();
		group.setName(context.getArgs()[0]);
		addGroup(group);
		
		return get(ReturnType.MESSAGE, "Added group!", source, context);
	}

	@Command(name = "gaddperm", desc = "adds perm to a group")
	public CommandResult gaddPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.add")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.TERMINALUSER ? "." : "") + "gaddperm <name> <perms>...";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null) {
			return get(ReturnType.MESSAGE, "Group does not exist!", source, context);
		}
		for (int i = 1; i <= context.getArgs().length - 1; i++) {
			String perm = (context.getArgs()[i]);
			if (perm.startsWith("-")) {
				group.addNegatedPerm(perm.substring(1));
			} else {
				group.addPerm(perm);
			}
		}
		return null;
	}

	@Command(name = "gremperm", desc = "removes perm from a group", aliases = {"grmperm", "gremoveperm", "gdelperm", "gdeleteperm"})
	public CommandResult gremPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.remove")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.TERMINALUSER ? "." : "") + "gremperm <name> <perms>...";
			return get(ReturnType.NOTICE, "", source, context);
		}
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null) {
			return get(ReturnType.MESSAGE, "Group does not exist!", source, context);
		}
		for (int i = 1; i <= context.getArgs().length - 1; i++) {
			String perm = (context.getArgs()[i]);
			if (perm.startsWith("-")) {
				group.removeNegatedPerm(perm.substring(1));
			} else {
				group.removePerm(perm);
			}
		}
		return null;
	}

	@Command(name = "glistperms", desc = "list perms of a group")
	public CommandResult glistPerms(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.list")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 1) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "listperms <name> ";
			return get(ReturnType.NOTICE, "", source, context);
		}
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null) {
			return get(ReturnType.MESSAGE, "Group does not exist!", source, context);
		}
		StringBuilder builder = new StringBuilder();
		for (String perm : group.getPerms()) {
			builder.append(perm);
			builder.append(newLine);
		}
		
		return get(ReturnType.MESSAGE, builder.toString(), source, context);
	}

	@Command(name = "gwildcardperm", desc = "Removes or gives the wildcard perm to a group")
	public CommandResult gwildcardPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.wildcard")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "gwildcardperm <name> true/false";
			return get(ReturnType.NOTICE, "", source, context);
		}
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null) {
			return get(ReturnType.MESSAGE, "Group does not exist!", source, context);
		}
		if (context.getArgs()[1].equalsIgnoreCase("true") || context.getArgs()[1].equalsIgnoreCase("t") || context.getArgs()[1].equalsIgnoreCase("1")) {
			group.setWildcardPerm(true);
			return get(ReturnType.MESSAGE, "Set WildcardPerm to true", source, context);
		} else if (context.getArgs()[1].equalsIgnoreCase("false") || context.getArgs()[1].equalsIgnoreCase("f") || context.getArgs()[1].equalsIgnoreCase("0")) {
			group.setWildcardPerm(false);
			return get(ReturnType.MESSAGE, "Set WildcardPerm to false", source, context);
		} else {
			return get(ReturnType.MESSAGE, "Invalid setting. Please use true or false.", source, context);
		}
	}

	@Command(name = "ghasperm", desc = "Checks if a group has perm")
	public CommandResult ghasPermCommand(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
			return null;
		}
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.hasperm")) {
			return get(ReturnType.NOTICE, "You do not have permission", source, context);
		}
		if (context.getArgs() == null || context.getArgs().length < 2) {
			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "ghasperm <name> <perm> [ignore wildcard true/false]";
			return get(ReturnType.NOTICE, body, source, context);
		}
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null) {
			return get(ReturnType.MESSAGE, "Group does not exist!", source, context);
		}
		boolean ignoreWildcard = false;
		if (context.getArgs().length >= 3) {
			if (context.getArgs()[2].equalsIgnoreCase("true") || context.getArgs()[1].equalsIgnoreCase("t") || context.getArgs()[1].equalsIgnoreCase("1")) {
				ignoreWildcard = true;
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append("Group '").append(group.getName()).append("' ");
		builder.append(group.hasPerm(context.getArgs()[1], ignoreWildcard) ? "has " : "does not have ");
		builder.append("permission '").append(context.getArgs()[1]).append("'");

		return get(ReturnType.MESSAGE, builder.toString(), source, context);
	}
}
