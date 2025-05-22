package org.traccar.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.traccar.model.Device;
import org.traccar.model.Group;


public class GroupTree {
    private static class TreeNode {
        private Group group;
        private Device device;
        private Collection<TreeNode> children = new HashSet<>();

        TreeNode(Group group) {
            this.group = group;
        }

        TreeNode(Device device) {
            this.device = device;
        }


        public int hashCode() {
            if (this.group != null) {
                return (int) this.group.getId();
            }
            return (int) this.device.getId();
        }


        public boolean equals(Object obj) {
            if (!(obj instanceof TreeNode)) {
                return false;
            }
            TreeNode other = (TreeNode) obj;
            if (other == this) {
                return true;
            }
            if (this.group != null && other.group != null)
                return (this.group.getId() == other.group.getId());
            if (this.device != null && other.device != null) {
                return (this.device.getId() == other.device.getId());
            }
            return false;
        }

        public Group getGroup() {
            return this.group;
        }

        public Device getDevice() {
            return this.device;
        }

        public void setParent(TreeNode parent) {
            if (parent != null) {
                parent.children.add(this);
            }
        }

        public Collection<TreeNode> getChildren() {
            return this.children;
        }
    }


    private final Map<Long, TreeNode> groupMap = new HashMap<>();


    public GroupTree(Collection<Group> groups, Collection<Device> devices) {
        for (Group group : groups) {
            this.groupMap.put(Long.valueOf(group.getId()), new TreeNode(group));
        }

        for (TreeNode node : this.groupMap.values()) {
            if (node.getGroup().getGroupId() != 0L) {
                node.setParent(this.groupMap.get(Long.valueOf(node.getGroup().getGroupId())));
            }
        }

        Map<Long, TreeNode> deviceMap = new HashMap<>();

        for (Device device : devices) {
            deviceMap.put(Long.valueOf(device.getId()), new TreeNode(device));
        }

        for (TreeNode node : deviceMap.values()) {
            if (node.getDevice().getGroupId() != 0L) {
                node.setParent(this.groupMap.get(Long.valueOf(node.getDevice().getGroupId())));
            }
        }
    }


    public Collection<Group> getGroups(long groupId) {
        Set<TreeNode> results = new HashSet<>();
        getNodes(results, this.groupMap.get(Long.valueOf(groupId)));
        Collection<Group> groups = new ArrayList<>();
        for (TreeNode node : results) {
            if (node.getGroup() != null) {
                groups.add(node.getGroup());
            }
        }
        return groups;
    }

    public Collection<Device> getDevices(long groupId) {
        Set<TreeNode> results = new HashSet<>();
        getNodes(results, this.groupMap.get(Long.valueOf(groupId)));
        Collection<Device> devices = new ArrayList<>();
        for (TreeNode node : results) {
            if (node.getDevice() != null) {
                devices.add(node.getDevice());
            }
        }
        return devices;
    }

    private void getNodes(Set<TreeNode> results, TreeNode node) {
        if (node != null)
            for (TreeNode child : node.getChildren()) {
                results.add(child);
                getNodes(results, child);
            }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\GroupTree.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */