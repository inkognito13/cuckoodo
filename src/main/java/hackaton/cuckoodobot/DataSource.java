package hackaton.cuckoodobot;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Dmitry Tarasov, Anastasia Yarunina(@crazzysun)
 *         Date: 03/18/2017
 *         Time: 08:54
 */
public class DataSource {
    private static final String GET_ISSUE = "select * from ISSUES LEFT JOIN REMINDERS on (ISSUES.ID = REMINDERS.ISSUE_ID) where ISSUES.ID=? and ISSUES.GROUP_ID=? ORDER BY ISSUES.ID";
    private static final String GET_ISSUES_FOR_GROUP = "select * from ISSUES LEFT JOIN REMINDERS on (ISSUES.ID = REMINDERS.ISSUE_ID) where ISSUES.GROUP_ID=? ORDER BY ISSUES.ID";
    private static final String GET_ISSUES_FOR_USER = "select * from ISSUES LEFT JOIN REMINDERS on (ISSUES.ID = REMINDERS.ISSUE_ID) where ISSUES.ASSIGNEE=? and ISSUES.GROUP_ID=? ORDER BY ISSUES.ID";
    private static final String ADD_ISSUE = "insert into ISSUES(ID,GROUP_ID,TEXT,ASSIGNEE,DONE) VALUES (?,?,?,?,?)";
    private static final String ADD_REMINDER = "insert into REMINDERS(ISSUE_ID,CREATED,TARGET) VALUES (?,?,?)";
    private static final String DONE_ISSUE = "update ISSUES set DONE=true where ID=?";
    private static final String DELETE_ISSUE = "delete from ISSUES where ID=?";
    private static final String DELETE_REMINDER = "delete from REMINDERS where ISSUE_ID=?";
    private static final String CHANGE_ASSIGNEE = "update ISSUES set ASSIGNEE=? where ID=?";
    
    private JdbcTemplate jdbcTemplate;

    public DataSource(javax.sql.DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    public Issue getIssue(Long id, long groupId) {
        try {
            return jdbcTemplate.queryForObject(
                    GET_ISSUE,
                    new Object[]{id, groupId},
                    new IssueRowMapper()
            );
        } catch (DataAccessException e) {
            System.err.println("Error getIssue id=" + id + " groupId=" + groupId);
            e.printStackTrace();
        }
        return null;
    }

    List<Issue> getIssueForGroup(long groupId) {
        try {
            return jdbcTemplate.query(
                    GET_ISSUES_FOR_GROUP,
                    new Object[]{groupId},
                    new IssueRowMapper()
            );
        } catch (DataAccessException e) {
            System.err.println("Error getIssueForGroup groupId=" + groupId);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    void addIssue(Issue issue) {
        try {
            issue.setId(System.currentTimeMillis());
            jdbcTemplate.update(
                    ADD_ISSUE,
                    issue.getId(),
                    issue.getGroupId(),
                    issue.getText(),
                    issue.getAssignee(),
                    issue.getDone()
            );

            if (issue.getReminder() != null) {
                jdbcTemplate.update(
                        ADD_REMINDER,
                        issue.getId(),
                        new Timestamp(issue.getReminder().getCreated().getTimeInMillis()),
                        new Timestamp(issue.getReminder().getTarget().getTimeInMillis())
                );
            }
        } catch (DataAccessException e) {
            System.err.println("Error addIssue issue=" + issue);
            e.printStackTrace();
        }
    }

    boolean doneIssue(int idx, long groupId) {
        try {
            List<Issue> issues = getIssueForGroup(groupId);
            for (int i = 0; i < issues.size(); i++) {
                if ((i + 1) == idx) {
                    updateIssueToDone(issues.get(i).getId());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error done issue idx=" + idx + " groupId=" + groupId);
            e.printStackTrace();
        }
        return false;
    }

    private void updateIssueToDone(long issueId) throws SQLException {
        jdbcTemplate.update(DONE_ISSUE, issueId);
    }

    Issue deleteIssue(int idx, long groupId) {
        try {
            List<Issue> issues = getIssueForGroup(groupId);
            for (int i = 0; i < issues.size(); i++) {
                if ((i + 1) == idx) {
                    deleteIssueById(issues.get(i).getId());
                    deleteReminderByIssueId(issues.get(i).getId());
                    return issues.get(i);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error delete issue idx=" + idx + " groupId=" + groupId);
            e.printStackTrace();
        }
        return null;
    }

    private void deleteIssueById(long issueId) throws SQLException {
        jdbcTemplate.update(DELETE_ISSUE, issueId);
    }

    private void deleteReminderByIssueId(long issueId) throws SQLException {
        jdbcTemplate.update(DELETE_REMINDER, issueId);
    }

    boolean assigneeIssue(int idx, String newUser, long groupId) {
        try {
            List<Issue> issues = getIssueForGroup(groupId);
            for (int i = 0; i < issues.size(); i++) {
                if ((i + 1) == idx) {
                    updateIssueAssignee(issues.get(i).getId(), newUser);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error assign issue idx=" + idx + " groupId=" + groupId + " newUser=" + newUser);
            e.printStackTrace();
        }
        return false;
    }

    private void updateIssueAssignee(long issueId, String newUser) throws SQLException {
        jdbcTemplate.update(CHANGE_ASSIGNEE, newUser, issueId);
    }

    List<Issue> getAllIssueForUser(String username, long groupId) {
        List<Issue> result = new ArrayList<>();
        try {
            return jdbcTemplate.query(
                    GET_ISSUES_FOR_USER,
                    new Object[]{username, groupId},
                    new IssueRowMapper()
            );
        } catch (DataAccessException e) {
            System.err.println("Error getAllIssueForUser username=" + username + " groupId=" + groupId);
            e.printStackTrace();
        }
        return result;
    }

    private Issue issueFromRS(ResultSet rs) throws SQLException {
        Issue issue = new Issue(
                rs.getLong("ISSUES.GROUP_ID"),
                rs.getString("ISSUES.TEXT"),
                rs.getString("ISSUES.ASSIGNEE")
        );
        issue.setId(rs.getLong("ISSUES.ID"));
        issue.setDone(rs.getBoolean("ISSUES.DONE"));
        if (rs.getLong("REMINDERS.ISSUE_ID") != 0) {
            Calendar target = Calendar.getInstance();
            target.setTimeInMillis(rs.getTimestamp("REMINDERS.TARGET").getTime());
            Calendar created = Calendar.getInstance();
            created.setTimeInMillis(rs.getTimestamp("REMINDERS.CREATED").getTime());
            Reminder reminder = new Reminder(created, target);
            issue.setReminder(reminder);
        }
        return issue;
    }

    private class IssueRowMapper implements RowMapper<Issue> {
        @Override
        public Issue mapRow(ResultSet resultSet, int i) throws SQLException {
            return issueFromRS(resultSet);
        }
    }
}
