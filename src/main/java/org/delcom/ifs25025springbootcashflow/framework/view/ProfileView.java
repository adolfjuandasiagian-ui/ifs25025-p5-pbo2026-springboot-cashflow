package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Pengaturan akun: nama lengkap, password, dan foto profil. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {

    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FileStorage fileStorage;

    private final TextField username = new TextField("Username");
    private final TextField role = new TextField("Role");
    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField newPassword = new PasswordField("Password baru");
    private final PasswordField confirm = new PasswordField("Konfirmasi password baru");
    private final Div avatarSlot = new Div();
    private final Div uploadSlot = new Div();
    private final Button saveButton = new Button("Simpan Perubahan", VaadinIcon.CHECK.create());
    private final ProgressBar progress = new ProgressBar();

    private ImageUpload photoUpload = new ImageUpload();
    private User user;

    public ProfileView(UserUseCase userUseCase, IUserRepository userRepository, FileStorage fileStorage) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;

        getElement().setAttribute("role", "main");
        setPadding(true);
        setSpacing(true);

        username.setReadOnly(true);
        role.setReadOnly(true);
        fullName.setMaxLength(200);
        newPassword.setHelperText("Kosongkan jika tidak ingin mengganti password");
        for (TextField field : new TextField[]{username, role, fullName}) {
            field.setWidthFull();
        }
        newPassword.setWidthFull();
        confirm.setWidthFull();

        progress.setIndeterminate(true);
        progress.setVisible(false);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> save());

        uploadSlot.add(photoUpload.component());
        VerticalLayout photoBox = new VerticalLayout(new Span("Foto profil (gambar maks 5 MB)"), uploadSlot);
        photoBox.setPadding(false);

        PageCard card = new PageCard(
                new HorizontalLayout(avatarSlot, photoBox),
                username, role, fullName, newPassword, confirm, progress, saveButton);
        card.setMaxWidth("640px");
        add(new H1("Profil Saya"), card);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        user = AuthSession.currentUser(userRepository);
        if (user == null) {
            UI.getCurrent().navigate(LoginView.class);
            return;
        }
        username.setValue(user.getUsername());
        role.setValue(user.isAdmin() ? "Admin" : "User");
        fullName.setValue(user.getFullName());
        showAvatar();
    }

    private void showAvatar() {
        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage(user.getPhotoPath());
        }
        avatar.setHeight("96px");
        avatar.setWidth("96px");
        avatarSlot.removeAll();
        avatarSlot.add(avatar);
    }

    private void save() {
        if (!newPassword.getValue().equals(confirm.getValue())) {
            Toasts.error("[!] Konfirmasi password tidak sama");
            return;
        }
        progress.setVisible(true);
        saveButton.setEnabled(false);
        String newPhoto = null;
        String cleanupWarning = null;
        try {
            String oldPhoto = user.getPhotoPath();
            if (photoUpload.hasFile()) {
                newPhoto = fileStorage.store(photoUpload.getFileName(), photoUpload.getData());
            }
            user = userUseCase.updateProfile(user.getId(), fullName.getValue(), newPassword.getValue());
            if (newPhoto != null) {
                user = userUseCase.updatePhoto(user.getId(), newPhoto);
                if (oldPhoto != null) {
                    cleanupWarning = deletePhoto(oldPhoto);
                }
            }
            resetForm();
            Toasts.success("Profil berhasil diperbarui.");
            if (cleanupWarning != null) {
                Toasts.error(cleanupWarning);
            }
        } catch (ValidationException | EntityNotFoundException ex) {
            cleanupWarning = newPhoto == null ? null : deletePhoto(newPhoto);
            Toasts.error("[!] " + ex.getMessage());
            if (cleanupWarning != null) {
                Toasts.error(cleanupWarning);
            }
        } catch (IOException ex) {
            Toasts.error("[!] Gagal menyimpan foto: " + ex.getMessage());
        } finally {
            progress.setVisible(false);
            saveButton.setEnabled(true);
        }
    }

    private String deletePhoto(String path) {
        try {
            return fileStorage.delete(path)
                    ? null
                    : "[!] Foto tidak ditemukan saat pembersihan.";
        } catch (UncheckedIOException ex) {
            return "[!] Gagal menghapus foto: " + ex.getMessage();
        }
    }

    /** Membersihkan input sensitif, memuat ulang avatar, dan menyegarkan header aplikasi. */
    private void resetForm() {
        newPassword.clear();
        confirm.clear();
        fullName.setValue(user.getFullName());
        photoUpload = new ImageUpload();
        uploadSlot.removeAll();
        uploadSlot.add(photoUpload.component());
        showAvatar();
        Component parent = this;
        while (parent != null) {
            if (parent instanceof MainLayout layout) {
                layout.refreshUser();
                break;
            }
            parent = parent.getParent().orElse(null);
        }
    }
}
