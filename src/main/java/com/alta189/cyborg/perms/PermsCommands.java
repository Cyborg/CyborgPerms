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

import com.alta189.cyborg.Cyborg;
import com.alta189.cyborg.api.command.CommandContext;
import com.alta189.cyborg.api.command.CommandSource;
import com.alta189.cyborg.api.command.annotation.Command;

import static com.alta189.cyborg.perms.PermissionManager.addGroup;
import static com.alta189.cyborg.perms.PermissionManager.getGroup;
import static com.alta189.cyborg.perms.PermissionManager.getUser;
import static com.alta189.cyborg.perms.PermissionManager.hasPerm;
import static com.alta189.cyborg.perms.PermissionManager.registerUser;

import org.pircbotx.User;

public class PermsCommands {

	@Command(name = "register", desc = "Register with CyborgPerms")
	public String register(CommandSource source, CommandContext context) {
		switch (source.getSource()) {
			case TERMINALUSER:
				return "Can't register from the terminal!";
			case USER:
				if (context.getPrefix() == null || !context.getPrefix().equals("."))
					return "";
				User user = source.getUser();
				if (context.getArgs() == null || context.getArgs().length < 2)
					return "Correct usage is .register <name> <password>";
				if (context.getLocationType() == null || context.getLocationType() != CommandContext.LocationType.PRIVATE_MESSAGE)
					return "You may only register via a private message!";
				if (user.getHostmask() == null)
					return "Your hostmask is null. Make sure you are in a channel with me";
				if (getUser(context.getArgs()[0]) != null)
					return "Someone with this nick has already registered";
				registerUser(user.getNick(), user.getLogin(), user.getHostmask(), context.getArgs()[1]);
				return "registered!";
		}
		return null;
	}
	
	@Command(name = "authenticate", desc = "Authenticates you with CyborgPerms", aliases = {"auth"})
	public String authenticate(CommandSource source, CommandContext context) {
		switch (source.getSource()) {
			case TERMINALUSER:
				return "Can't authenticate from the terminal!";
			case USER:
				if (context.getPrefix() == null || !context.getPrefix().equals("."))
					return null;
				if (context.getArgs() == null || context.getArgs().length < 2)
					return "Correct usage is .authenticate <name> <password>";
				if (context.getLocationType() == null || context.getLocationType() != CommandContext.LocationType.PRIVATE_MESSAGE)
					return "You may only authenticate via a private message!";
				CyborgUser user = getUser(context.getArgs()[0]);
				if (user == null)
					return "User does not exist!";
				if (user.authenticatePassword(context.getArgs()[1])) {
					user.addTempHostame(source.getUser().getLogin() + "@" + source.getUser().getHostmask());
					return "Authenticated!";
				} else {
					return "Incorrect password!";
				}
		}
		return null;
	}

	@Command(name = "addhostname", desc = "Adds a hostname to a CyborgPerms account", aliases = {"addhost", "addhostmask"})
	public String addHostname(CommandSource source, CommandContext context) {
		switch (source.getSource()) {
			case TERMINALUSER:
				return "Can't register from the terminal!";
			case USER:
				if (context.getPrefix() == null || !context.getPrefix().equals("."))
					return null;
				if (context.getArgs() == null || context.getArgs().length < 2)
					return "Correct usage is .addhostname <name> <password>";
				if (context.getLocationType() == null || context.getLocationType() != CommandContext.LocationType.PRIVATE_MESSAGE)
					return "You may only register via a private message!";
				CyborgUser user = getUser(context.getArgs()[0]);
				if (user == null)
					return "User does not exist!";
				if (source.getUser().getHostmask() == null)
					return "Your hostmask is null. Make sure you are in a channel with me";
				if (user.authenticatePassword(context.getArgs()[1])) {
					user.addHostname(source.getUser().getLogin() + "@" + source.getUser().getHostmask());
					return "Added hostname!";
				} else {
					return "Incorrect password!";
				}

		}
		return null;
	}

	@Command(name = "addperm", desc = "adds perm to a user")
	public String addPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.add"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.TERMINALUSER ? "." : "") + "addperm <name> <perms>...";
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null)
			return "User does not exist!";
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
	public String remerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.remove"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.TERMINALUSER ? "." : "") + "remperm <name> <perms>...";
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null)
			return "User does not exist!";
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
	public String listPerms(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.list"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 1)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "listperms <name> ";
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null)
			return "User does not exist!";
		StringBuilder builder = new StringBuilder();
		for (String perm : user.getPerms()) {
			builder.append(perm);
			builder.append("\r\n");
		}
		return builder.toString();
	}
	
	@Command(name = "wildcardperm", desc = "Removes or gives the wildcard perm to a user")
	public String wildcardPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.wildcard"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "wildcardperm <name> true/false";
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null)
			return "User does not exist!";
		if (context.getArgs()[1].equalsIgnoreCase("true") || context.getArgs()[1].equalsIgnoreCase("t") || context.getArgs()[1].equalsIgnoreCase("1")) {
			user.setWildcardPerm(true);
			return "Set WildcardPerm to true";
		} else if (context.getArgs()[1].equalsIgnoreCase("false") || context.getArgs()[1].equalsIgnoreCase("f") || context.getArgs()[1].equalsIgnoreCase("0")) {
			user.setWildcardPerm(false);
			return "Set WildcardPerm to false";
		} else {
			return "Invalid setting. Please use true or false.";
		}
	}
	
	@Command(name = "userinfo", desc = "Returns info on a user")
	public String userInfo(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.userinfo"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 1)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "userinfo <name>";
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null)
			return "User '" + context.getArgs()[0] + "' does not exist!";
		StringBuilder builder = new StringBuilder();
		builder.append("name: ").append(user.getName()).append("\r\n");
		builder.append("wildcard: ").append(user.hasWildcardPerm()).append("\r\n");
		builder.append("hostnames: ");
		int i = 0;
		for (String hostname : user.getRawHostnames()) {
			i ++;
			builder.append(hostname);
			if (i != user.getRawHostnames().size())
				builder.append(", ");
		}
		builder.append("\r\n");
		builder.append("groups: ");
		i = 0;
		for (String group : user.getGroups()) {
			i ++;
			builder.append(group);
			if (i != user.getRawHostnames().size())
				builder.append(", ");
		}
		return builder.toString();
	}

	@Command(name = "hasperm", desc = "Checks if a user has perm")
	public String hasPermCommand(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.hasperm"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "hasperm <name> <perm> [ignore wildcard true/false]";
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null)
			return "User does not exist!";
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
		return builder.toString();
	}

	@Command(name = "addgroup", desc = "Adds a user to a group")
	public String userAddGroup(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "addgroup.hasperm"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "hasperm <name> <group> ";
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null)
			return "User does not exist!";
		if (getGroup(context.getArgs()[1]) == null)
			return "Group does not exist!";
		user.addGroup(context.getArgs()[1]);
		return "Added user to group!";
	}

	@Command(name = "remgroup", desc = "Removes a user from a group")
	public String userRemGroup(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "addgroup.hasperm"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "hasperm <name> <group> ";
		CyborgUser user = getUser(context.getArgs()[0]);
		if (user == null)
			return "User does not exist!";
		user.removeGroup(context.getArgs()[1]);
		return "Removed user from group!";
	}
	
	//Group Commands

	@Command(name = "gadd", desc = "Adds a new group")
	public String addGroupCommand(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.gadd"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 1)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "gadd <name>";
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group != null) 
			return "Group already exists!";
		group = new CyborgGroup();
		group.setName(context.getArgs()[0]);
		addGroup(group);
		return "Added group!";
	}

	@Command(name = "gaddperm", desc = "adds perm to a group")
	public String gaddPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.add"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.TERMINALUSER ? "." : "") + "gaddperm <name> <perms>...";
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null)
			return "Group does not exist!";
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
	public String gremPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.remove"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.TERMINALUSER ? "." : "") + "gremperm <name> <perms>...";
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null)
			return "Group does not exist!";
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
	public String glistPerms(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.list"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 1)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "listperms <name> ";
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null)
			return "Group does not exist!";
		StringBuilder builder = new StringBuilder();
		for (String perm : group.getPerms()) {
			builder.append(perm);
			builder.append("\r\n");
		}
		return builder.toString();
	}

	@Command(name = "gwildcardperm", desc = "Removes or gives the wildcard perm to a group")
	public String gwildcardPerm(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.wildcard"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "gwildcardperm <name> true/false";
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null)
			return "Group does not exist!";
		if (context.getArgs()[1].equalsIgnoreCase("true") || context.getArgs()[1].equalsIgnoreCase("t") || context.getArgs()[1].equalsIgnoreCase("1")) {
			group.setWildcardPerm(true);
			return "Set WildcardPerm to true";
		} else if (context.getArgs()[1].equalsIgnoreCase("false") || context.getArgs()[1].equalsIgnoreCase("f") || context.getArgs()[1].equalsIgnoreCase("0")) {
			group.setWildcardPerm(false);
			return "Set WildcardPerm to false";
		} else {
			return "Invalid setting. Please use true or false.";
		}
	}

	@Command(name = "ghasperm", desc = "Checks if a group has perm")
	public String ghasPermCommand(CommandSource source, CommandContext context) {
		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals(".")))
			return null;
		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "perms.hasperm"))
			return "You do not have permission";
		if (context.getArgs() == null || context.getArgs().length < 2)
			return "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "ghasperm <name> <perm> [ignore wildcard true/false]";
		CyborgGroup group = getGroup(context.getArgs()[0]);
		if (group == null)
			return "Group does not exist!";
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
		return builder.toString();
	}

	@Command(name = "rage", desc = "test")
	public String test(CommandSource source, CommandContext context) {
		if (context.getPrefix() == null || !context.getPrefix().equals("."))
			return null;
		Cyborg.getInstance().shutdown();
		return "exited!" + Cyborg.getInstance().getMessageDelay();
	}

}
