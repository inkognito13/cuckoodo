package hackaton.cuckoodobot;

import java.util.*;

/**
 * @author Dmitry Tarasov, Anastasia Yarunina(@crazzysun)
 *         Date: 03/18/2017
 *         Time: 08:54
 */
public class DataSource {
    //<groupId:<issueId:issue>>
    Map<Long, Map<Long, Issue>> storage = new HashMap<Long, Map<Long, Issue>>();

    public Issue getIssue(Long id, long groupId) {
        if (storage.containsKey(groupId)) {
            Map<Long, Issue> groupMessages = storage.get(groupId);
            if (groupMessages.containsKey(id)) {
                return groupMessages.get(id);
            }
        }

        return null;
    }

    List<Issue> getIssueForGroup(long groupId) {
        Map<Long, Issue> groupMessages = storage.get(groupId);
        if (groupMessages == null) {
            groupMessages = new TreeMap<Long, Issue>();
            storage.put(groupId, groupMessages);
        }
        return new ArrayList<Issue>(groupMessages.values());
    }

    Issue addIssue(Issue issue) {
        Map<Long, Issue> groupMessages = storage.get(issue.getGroupId());
        if (groupMessages == null) {
            groupMessages = new TreeMap<Long, Issue>();
            storage.put(issue.getGroupId(), groupMessages);
        }
        groupMessages.put(issue.getId(), issue);
        return issue;
    }

    boolean doneIssue(int idx, long groupId) {
        if (storage.containsKey(groupId)) {
            Map<Long, Issue> groupMessages = storage.get(groupId);

            for (Long key : groupMessages.keySet()) {
                idx--;
                if (idx == 0) {
                    groupMessages.get(key).setDone(true);
                    return true;
                }
            }
        }

        return false;
    }

    Issue deleteIssue(int idx, long groupId) {
        if (storage.containsKey(groupId)) {
            Map<Long, Issue> groupMessages = storage.get(groupId);

            if (groupMessages.size() >= idx) {
                for (Long key : groupMessages.keySet()) {
                    idx--;
                    if (idx == 0) {
                        return groupMessages.remove(key);
                    }
                }
            }
        }
        return null;
    }

    boolean assigneeIssue(int idx, String newUser, long groupId) {
        if (storage.containsKey(groupId)) {
            Map<Long, Issue> groupMessages = storage.get(groupId);

            if (groupMessages.size() >= idx) {
                for (Long key : groupMessages.keySet()) {
                    idx--;
                    if (idx == 0) {
                        groupMessages.get(key).setAssignee(newUser);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    ArrayList<Issue> getAllIssueForUser(String username, long groupId) {
        ArrayList<Issue> res = new ArrayList<Issue>();
        if (storage.containsKey(groupId)) {
            Map<Long, Issue> groupMessages = storage.get(groupId);

            for (Issue issue : groupMessages.values()) {
                if (issue.getAssignee().toLowerCase().equals(username.toLowerCase())) {
                    res.add(issue);
                }
            }
        }

        return res;
    }

}
