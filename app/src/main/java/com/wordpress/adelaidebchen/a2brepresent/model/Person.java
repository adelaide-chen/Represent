package com.wordpress.adelaidebchen.a2brepresent.model;

public class Person {

    private String personName;
    private String party;
    private String link;
    private String email;
    private String moreInfo;
    private String bioguide_id;

    public Person(String personName, String party, String link, String email, String moreInfo, String bioguide_id) {
        this.personName = personName;
        this.party = party;
        this.link = link;
        this.email = email;
        this.moreInfo = moreInfo;
        this.bioguide_id = bioguide_id;
    }

    public String getPersonName() {
        return personName;
    }

    public String getParty() {
        return party;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public String getLink() {
        return link;
    }

    public String getEmail() {
        return email;
    }

    public String getBioguide_id() {
        return bioguide_id;
    }

}