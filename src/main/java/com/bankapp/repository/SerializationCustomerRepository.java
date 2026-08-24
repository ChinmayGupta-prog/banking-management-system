package com.bankapp.repository;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class SerializationCustomerRepository implements CustomerRepository {
    private final Path dataFile;

    public SerializationCustomerRepository(Path dataFile) {
        this.dataFile = dataFile;
    }

    @Override
    public BankData load() {
        if (Files.notExists(dataFile)) return new BankData();
        try (var input = new ObjectInputStream(Files.newInputStream(dataFile))) {
            Object stored = input.readObject();
            if (stored instanceof BankData data) return data;
            throw new DataAccessException("Unsupported data in " + dataFile + ".", null);
        } catch (EOFException e) {
            throw new DataAccessException("Data file is empty or incomplete: " + dataFile + ".", e);
        } catch (IOException | ClassNotFoundException e) {
            throw new DataAccessException("Could not read " + dataFile + "; it may be corrupt or incompatible.", e);
        }
    }

    @Override
    public void save(BankData data) {
        Path absoluteFile = dataFile.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Path temporaryFile = parent.resolve(absoluteFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(parent);
            try (var output = new ObjectOutputStream(Files.newOutputStream(temporaryFile))) {
                output.writeObject(data);
            }
            try {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try { Files.deleteIfExists(temporaryFile); } catch (IOException ignored) { }
            throw new DataAccessException("Could not save data to " + dataFile + ".", e);
        }
    }
}
