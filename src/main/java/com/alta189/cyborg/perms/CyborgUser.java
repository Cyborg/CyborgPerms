package com.alta189.cyborg.perms;

import com.alta189.simplesave.Field;
import com.alta189.simplesave.Id;
import com.alta189.simplesave.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

@Table("permsusers")
public class CyborgUser {
	@Id
	private int id;
	@Field
	private String name;
	@Field
	private String password;
	@Field
	private String rawHostnames;
	@Field
	private boolean wildcardPerm = false;
	@Field
	private String rawNegatedPerms;
	@Field
	private String rawPerms;
	@Field
	private String rawGroups;
	// Non-persistence data
	private final List<String> hostnames = new ArrayList<String>();
	private final List<String> tempHostnames = new ArrayList<String>();
	private final List<String> negatedperms = new ArrayList<String>();
	private final List<String> perms = new ArrayList<String>();
	private final List<String> groups = new ArrayList<String>();

	protected void load() {
		if (rawNegatedPerms != null && !rawNegatedPerms.isEmpty() && !rawNegatedPerms.trim().equals(";")) {
			Collections.addAll(negatedperms, rawNegatedPerms.split(";"));
		}

		if (rawPerms != null && !rawPerms.isEmpty() && !rawPerms.trim().equals(";")) {
			Collections.addAll(perms, rawPerms.split(";"));
		}

		if (rawHostnames != null && !rawHostnames.isEmpty() && !rawHostnames.trim().equals(";")) {
			Collections.addAll(hostnames, rawHostnames.split(";"));
		}

		if (rawGroups != null && !rawGroups.isEmpty() && !rawGroups.trim().equals(";")) {
			Collections.addAll(groups, rawGroups.split(";"));
		}
	}

	protected void flush() {
		StringBuilder builder = new StringBuilder();
		for (String perm : negatedperms) {
			builder.append(perm).append(";");
		}

		if (builder.toString() == null || builder.toString().isEmpty() || builder.toString().trim().equals(";")) {
			rawNegatedPerms = "";
		} else {
			rawNegatedPerms = builder.toString();
		}

		builder = new StringBuilder();
		for (String perm : perms) {
			builder.append(perm).append(";");
		}
		if (builder.toString() == null || builder.toString().isEmpty() || builder.toString().trim().equals(";")) {
			rawPerms = "";
		} else {
			rawPerms = builder.toString();
		}

		builder = new StringBuilder();
		for (String hostname : hostnames) {
			builder.append(hostname).append(";");
		}
		if (builder.toString() == null || builder.toString().isEmpty() || builder.toString().trim().equals(";")) {
			rawHostnames = "";
		} else {
			rawHostnames = builder.toString();
		}

		builder = new StringBuilder();
		for (String group : groups) {
			builder.append(group).append(";");
		}
		if (builder.toString() == null || builder.toString().isEmpty() || builder.toString().trim().equals(";")) {
			rawGroups = "";
		} else {
			rawGroups = builder.toString();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CyborgUser)) {
			return false;
		}

		CyborgUser user = (CyborgUser) o;
		return (name.equalsIgnoreCase(name) && password.equalsIgnoreCase(password));
	}

	public void setPassword(String password) {
		this.password = String.valueOf(Hex.encodeHex(DigestUtils.sha(password)));
	}

	public boolean authenticatePassword(String password) {
		return String.valueOf(Hex.encodeHex(DigestUtils.sha(password))).equals(this.password);
	}

	public boolean authenticateHostname(String hostname) {
		return hostnames.contains(hostname.toLowerCase());
	}

	public boolean hasPerm(String perm) {
		return hasPerm(perm, false);
	}

	public boolean hasPerm(String perm, boolean ignoreWildcard) {
		perm = perm.toLowerCase();
		if (hasNegatedPerm(perm)) {
			return false;
		}

		if (!ignoreWildcard && wildcardPerm) {
			return true;
		}

		if (perms.contains(perm)) {
			return true;
		}

		for (String g : groups) {
			CyborgGroup group = PermissionManager.getGroup(g);
			if (group != null && group.hasPerm(perm)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasNegatedPerm(String perm) {
		return negatedperms.contains(perm.toLowerCase());
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasWildcardPerm() {
		return wildcardPerm;
	}

	public void setWildcardPerm(boolean wildcardPerm) {
		this.wildcardPerm = wildcardPerm;
	}

	public void addPerm(String perm) {
		perms.add(perm.toLowerCase());
	}

	public void removePerm(String perm) {
		perms.remove(perm.toLowerCase());
	}

	public List<String> getPerms() {
		return perms;
	}

	public void addNegatedPerm(String perm) {
		negatedperms.add(perm.toLowerCase());
	}

	public void removeNegatedPerm(String perm) {
		perms.remove(perm.toLowerCase());
	}

	public List<String> getNegatedPerms() {
		return negatedperms;
	}

	public void addGroup(String group) {
		groups.add(group.toLowerCase());
	}

	public void removeGroup(String group) {
		groups.remove(group.toLowerCase());
	}

	public List<String> getGroups() {
		return groups;
	}

	public List<String> getHostnames() {
		List<String> result = new ArrayList<String>();
		result.addAll(hostnames);
		result.addAll(tempHostnames);
		return result;
	}

	public List<String> getRawHostnames() {
		return hostnames;
	}

	public void addHostname(String hostname) {
		hostnames.add(hostname);
	}

	public List<String> getTempHostnames() {
		return tempHostnames;
	}

	public void addTempHostame(String hostname) {
		tempHostnames.add(hostname);
	}
}
