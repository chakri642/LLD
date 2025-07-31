import java.util.*;
import java.util.concurrent.*;

class Experience {
    String title, companyName, duration;

    Experience(String title, String companyName, String duration) {
        this.title = title;
        this.companyName = companyName;
        this.duration = duration;
    }
}

class Education {
    String degree, school, year;

    Education( String school, String degree, String year) {
        this.degree = degree;
        this.school = school;
        this.year = year;
    }
}

class Skill {
    String name;

    Skill(String name) {
        this.name = name;
    }
}

class Profile {
    String picture, headline, summary;
    List<Experience> experiences = new ArrayList<>();
    List<Education> educations = new ArrayList<>();
    List<Skill> skills = new ArrayList<>();
}

class Message {
    String id, content;
    UserL sender, receiver;
    Date timestamp = new Date();

    Message(String id, String content, UserL sender, UserL receiver) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
    }
}

class JobPosting {
    String id, title, company, location, description;
    Date postedDate = new Date();

    public  JobPosting(String id, String title, String company, String location, String description) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
    }
}

class UserL {
    String id, name, email, password;
    Profile profile = new Profile();
    List<UserL> connections = new ArrayList<>();
    List<Message> messages = new ArrayList<>();
    List<Message> inbox = new ArrayList<>();

    UserL(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public void addExperience(Experience experience) {
        profile.experiences.add(experience);
    }

    public void addEducation(Education education) {
        profile.educations.add(education);
    }

    public void addSkill(Skill skill) {
        profile.skills.add(skill);
    }

    public void sendMessage(Message message) {
        messages.add(message);
    }

//    public void postJob(JobPosting jobPosting) {
//        jobPostings.add(jobPosting);
//    }
}

enum NotificationType {
    MESSAGE, CONNECTION_REQUEST, JOB_POSTING
}

class Notification {
    String id, content;
    UserL sender;
    NotificationType type;
    Date timestamp = new Date();

    public Notification(String id, UserL sender, NotificationType type, String content) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.type = type;
    }
}

class LinkedInService {
    private static LinkedInService instance;
    private static final Object lock = new Object();

    private final ConcurrentHashMap<String, UserL> users = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<JobPosting> jobPostings = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, List<Notification>> notifications = new ConcurrentHashMap<>();

    private LinkedInService () {
        // Private constructor to prevent instantiation
    }

    public static LinkedInService getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new LinkedInService();
            }
        }
        return instance;
    }

    public UserL registerUser(String id, String name, String email, String password) {
        UserL user = new UserL(id, name, email, password);
        users.put(id, user);
        return user;
    }

    public UserL login(String email, String password) {
        for (UserL user : users.values()) {
            if (user.email.equals(email) && user.password.equals(password)) {
                return user;
            }
        }
        return null; // Invalid credentials
    }

    public void updateProfile(UserL u, Profile p){
        u.profile = p;
    }

    public void sendConnectionRequest(UserL sender ,UserL receiver){
        addNotification(receiver, new Notification(UUID.randomUUID().toString(), receiver, NotificationType.CONNECTION_REQUEST, sender.name + " sent you a connection request."));
        System.out.println("Connection request sent from " + sender.name + " to " + receiver.name);
    }

    public void acceptConnection(UserL sender, UserL receiver) {
        sender.connections.add(receiver);
        receiver.connections.add(sender);
        System.out.println(receiver.name + " accepted connection request from " + sender.name);
    }

    public void sendMessage(UserL sender, UserL receiver, String content) {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(messageId, content, sender, receiver);
        sender.sendMessage(message);
        receiver.inbox.add(message);
        addNotification(receiver, new Notification(messageId, sender, NotificationType.MESSAGE, content));
        System.out.println("Message sent from " + sender.name + " to " + receiver.name);
    }

    public void postJob(JobPosting jobPosting) {
        String jobId = UUID.randomUUID().toString();
        jobPostings.add(jobPosting);
        for(UserL user : users.values()) {
            addNotification(user, new Notification(jobId, user, NotificationType.JOB_POSTING, "You posted a new job: " + jobPosting.title));
            System.out.println("Job posted by " + user.name + ": " + jobPosting.title);
        }
    }

    public List<UserL> searchUsers(String keyword) {
        List<UserL> result = new ArrayList<>();
        for (UserL user : users.values()) {
            if (user.name.toLowerCase().contains(keyword.toLowerCase()) || user.email.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(user);
            }
        }
        return result;
    }

    public List<JobPosting> searchJobs(String keyword) {
        List<JobPosting> result = new ArrayList<>();
        for (JobPosting job : jobPostings) {
            if (job.title.toLowerCase().contains(keyword.toLowerCase()) || job.description.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(job);
            }
        }
        return result;
    }

    public List<Notification> getNotifications(UserL user) {
        return notifications.getOrDefault(user.id, new ArrayList<>());
    }

    private void addNotification(UserL user, Notification notification) {
//      notifications.computeIfAbsent(user.id, k -> new ArrayList<>()).add(notification);
        notifications.putIfAbsent(user.id, new ArrayList<>());
        notifications.get(user.id).add(notification);
    }

}


public class LinkedInDemo {
    public static void main(String[] args) {
        LinkedInService service = LinkedInService.getInstance();

        UserL alice = service.registerUser("1", "Alice", "alice@pro.com", "pass");
        UserL bob = service.registerUser("2", "Bob", "bob@pro.com", "pass");

        service.sendConnectionRequest(alice, bob);
        service.acceptConnection(alice, bob);

        Profile aliceProfile = new Profile();
        aliceProfile.headline = "Software Engineer";
        aliceProfile.skills.add(new Skill("Java"));
        service.updateProfile(alice, aliceProfile);

        service.postJob(new JobPosting("101", "Java Developer", "Exciting Java role", "Java, Spring", "Remote"));
        service.sendMessage(alice, bob, "Hi Bob, let’s connect!");

        System.out.println("Alice’s connections: " + alice.connections.size());
        System.out.println("Bob’s inbox: " + bob.messages.size());
        System.out.println("Jobs matching ‘Java’: " + service.searchJobs("Java").size());
        System.out.println("Notifications for Bob:");
        for (Notification n : service.getNotifications(bob)) {
            System.out.println(" - " + n.content);
        }
    }
}
