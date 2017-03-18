package hackaton.cuckoodobot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Dmitry Tarasov, Anastasia Yarunina
 *         Date: 03/18/2017
 *         Time: 08:54
 */
public class DataSource {
    //<groupId:<issueId:issue>>
    Map<Long, Map<Long, Issue>> storage = new HashMap<Long, Map<Long, Issue>>();

    public Issue getIssue(Long id, Long groupId) {
        if (storage.containsKey(groupId)) {
            Map<Long, Issue> groupMessages = storage.get(groupId);
            if (groupMessages.containsKey(id)) {
                return groupMessages.get(id);
            }
        }

        return null;
    }

    public List<Issue> getIssueForGroup(Long groupId) {
        Map<Long, Issue> groupMessages = storage.get(groupId);
        if (groupMessages == null) {
            groupMessages = new TreeMap<Long, Issue>();
            storage.put(groupId, groupMessages);
        }
        return new ArrayList<Issue>(groupMessages.values());
    }

    public Issue addIssue(Issue issue) {
        Map<Long, Issue> groupMessages = storage.get(issue.getGroupId());
        if (groupMessages == null) {
            groupMessages = new TreeMap<Long, Issue>();
            storage.put(issue.getGroupId(), groupMessages);
        }
        groupMessages.put(issue.getId(), issue);
        return issue;
    }

    public boolean doneIssue(int idx, Long groupId) {
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

    public Issue deleteIssue(int idx, long groupId) {
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
}
