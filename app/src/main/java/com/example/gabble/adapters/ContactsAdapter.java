package com.example.gabble.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gabble.databinding.ItemContainerContactsBinding;
import com.example.gabble.listeners.ContactListener;
import com.example.gabble.models.Contact;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>{

    private final List<Contact> contactList;
    private final ContactListener contactListener;

    public ContactsAdapter(List<Contact> contactList, ContactListener contactListener) {
        this.contactList = contactList;
        this.contactListener = contactListener;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerContactsBinding itemContainerContactsBinding = ItemContainerContactsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ContactsViewHolder(itemContainerContactsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        holder.setContactsData(contactList.get(position));
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    class ContactsViewHolder extends RecyclerView.ViewHolder {

        ItemContainerContactsBinding binding;

        public ContactsViewHolder(@NonNull ItemContainerContactsBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        void setContactsData(Contact contact) {
            binding.textName.setText(contact.name);
            binding.textNumber.setText(contact.mobile);
            binding.getRoot().setOnClickListener(v -> contactListener.onContactClicked(contact));
        }
    }
}
