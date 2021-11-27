package com.example.socialmediaapp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.Adapter.UserAdapter;
import com.example.socialmediaapp.AccountActivity;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.PostActivity;
import com.example.socialmediaapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ContactsFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private UserAdapter userAdapter;
    private List<User> userList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show menu option in fragment

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Contacts");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList, true);
        recyclerView.setAdapter(userAdapter);

        getAllUsers();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        // hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                // if search query is not empty, then search
                onQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                onQuery(newText);
                return false;
            }

            private void onQuery(String newText) {
                // called whenever user press any single letter
                if (!TextUtils.isEmpty(newText.trim())) {
                    //search text contains text, search it
                    searchUsers(newText);
                } else {
                    // search text empty, get all users
                    getAllUsers();
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                firebaseAuth.signOut();
                checkUserStatus();
                break;
            case R.id.action_add_post:
                startActivity(new Intent(getActivity(), PostActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getAllUsers() {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    // get all users except the current user
                    if (!user.getUserID().equals((firebaseUser.getUid()))) {
                        userList.add(user);
                    }
                }
                Collections.reverse(userList);

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(String s) {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("userName")
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (!user.getUserID().equals((firebaseUser.getUid()))) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getActivity(), AccountActivity.class));
            getActivity().finish();
        } else {
            //stay in current page
        }
    }
}