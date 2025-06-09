import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


class User {
    int id;
    String name;
    String email;
    int reputation;

    static int idCounter = 1;
    User(String name, String email) {
        this.id = idCounter++;
        this.name = name;
        this.email = email;
        this.reputation = 0;
    }

    void increaseReputation(int points) {
        this.reputation += points;
    }
}

class Tag {
    int id;
    String name;

    static int idCounter = 1;
    Tag(String name) {
        this.id = idCounter++;
        this.name = name;
    }
}

class Comment {
    int id;
    String content;
    User author;
    Date creationDate;

    static int idCounter = 1;
    Comment(String content, User author) {
        this.id = idCounter++;
        this.content = content;
        this.author = author;
        this.creationDate = new Date();
    }
}

class Vote {
    int id;
    User user;
    Boolean isUpvote;

    static int idCounter = 1;
    Vote(User user, boolean isUpvote) {
        this.id = idCounter++;
        this.user = user;
        this.isUpvote = isUpvote;
    }

}


class Answer {
    int id;
    String content;
    User author;
    Date creationDate;
    List<Comment> comments;
    List<Vote> votes;
    Question question;

    static int idCounter = 1;
    Answer(String content, User author, Question question) {
        this.id = idCounter++;
        this.content = content;
        this.author = author;
        this.creationDate = new Date();
        this.comments = new ArrayList<>();
        this.votes = new ArrayList<>();
        this.question = question;
        question.addAnswer(this);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void addVote(Vote vote) {
        this.votes.add(vote);
    }
}


class Question {
    int id;
    String title;
    String content;
    User author;
    Date creationDate;
    List <Tag> tags;
    List <Comment> comments;
    List <Vote> votes;
    List<Answer> answers;

    static int idCounter = 1;
    Question(String title, String content, User author){
        this.id = idCounter++;
        this.title = title;
        this.content = content;
        this.author = author;
        this.creationDate = new Date();
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.votes = new ArrayList<>();
        this.answers = new ArrayList<>();
    }

    public void addTags(Tag tag){
        this.tags.add(tag);
    }

    public void addAnswer(Answer answer){
        this.answers.add(answer);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
    public void addVote(Vote vote) {
        this.votes.add(vote);
    }

}

class StackOverflow {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Question> questions = new HashMap<>();
    private final Map<Integer, Tag> tags = new HashMap<>();
    static int idCounter = 1;
    private final ReentrantLock lock = new ReentrantLock();

    public User createUser(String name, String email){
        lock.lock();
        try{
            User user = new User(name,email);
            users.put(user.id,user);
            return user;
        }
        finally {
            lock.unlock();
        }
    }

    public Question postQuestion(String title, String content, User author, List<String> tagNames){
        lock.lock();
        try{
            Question question = new Question(title, content, author);

            for(String tagName: tagNames){
                Tag tempTag = null;
                Boolean flag=false;
                for(Map.Entry<Integer,Tag> tag: tags.entrySet()){
                    if(tag.getValue().name.equals(tagName)){
                        tempTag = tag.getValue();
                        flag=true;
                        break;
                    }
                }
                if(!flag) {
                    tempTag = new Tag(tagName);
                }
                question.addTags(tempTag);
            }

            questions.put(question.id, question);
            return question;
        }
        finally {
            lock.unlock();
        }
    }

    public Answer postAnswer(String content, User author, Question question){
        lock.lock();
        try{
            Answer ans = new Answer(content, author, question);
            question.addAnswer(ans);
            return ans;
        }
        finally {
            lock.unlock();
        }
    }

    public Comment postComment(String content, User author) {
        lock.lock();
        try {
            return new Comment(content, author);
        } finally {
            lock.unlock();
        }
    }

    public void addCommentToQuestion(Comment c, Question q) {
        lock.lock();
        try {
            q.addComment(c);
        }
        finally {
            lock.unlock();
        }
    }

    public void addCommentToAnswer(Comment c, Answer a) {
        lock.lock();
        try {
            a.addComment(c);
        }
        finally {
            lock.unlock();
        }
    }

    public void voteQuestion(User user, Question question, Boolean isUpVote){
        lock.lock();
        try{
            Vote vote = new Vote(user,isUpVote);
            question.addVote(vote);
            question.author.increaseReputation(isUpVote ? 10 : -2);
        }
        finally {
            lock.unlock();
        }
    }

    public void voteAnswer(User user, Answer answer, Boolean isUpVote){
        lock.lock();
        try{
            Vote vote = new Vote(user,isUpVote);
            answer.addVote(vote);
            answer.author.increaseReputation(isUpVote ? 10 : -2);
        }
        finally {
            lock.unlock();
        }
    }

    public List<Question> searchQuestions(String keyword) {
        lock.lock();
        try{
            List<Question> result = new ArrayList<>();
            for(Question q: questions.values()){
                if(q.content.contains(keyword) || q.title.contains(keyword)){
                    result.add(q);
                }
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    public List<Question> getQuestionsByTag(String tagName){
        lock.lock();
        try {
            List<Question> result = new ArrayList<>();
            for (Question q : questions.values()) {
                for(Tag t:q.tags){
                    if(t.name.equals(tagName)){
                        result.add(q);
                        break;
                    }
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public List<Question> getQuestionsByUser(User user) {
        lock.lock();
        try {
            List<Question> result = new ArrayList<>();
            for (Question q : questions.values()) {
                if (q.author == user) result.add(q);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }


}



public class StackOverflowDemo {
    public static void main(String[] args) {
        StackOverflow so = new StackOverflow();
        User u1 = so.createUser("Alice", "alice@example.com");
        User u2 = so.createUser("Bob", "bob@example.com");
        Question q1 = so.postQuestion("What is Java?", "Explain Java basics.", u1, Arrays.asList("java", "programming"));
        Answer a1 = so.postAnswer("Java is a language.", u2, q1);

        Comment c1 = so.postComment("Thanks for the answer!", u1);
        so.addCommentToAnswer(c1, a1);

        so.voteQuestion(u2, q1, true);
        so.voteAnswer(u1, a1, true);

        System.out.println("Alice's reputation: " + u1.reputation);
        System.out.println("Bob's reputation: " + u2.reputation);

        List<Question> javaQs = so.searchQuestions("Java");
        System.out.println("Questions found: " + javaQs.size());
    }
}
