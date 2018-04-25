package be.nokorbis.commandsigns.updater.data.v16x;

import be.nokorbis.commandsigns.updater.data.ICommandBlockSaver;
import be.nokorbis.commandsigns.updater.models.CommandBlock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nokorbis on 22/01/2016.
 */
public class JsonCommandBlockSaver implements ICommandBlockSaver
{
    private static final String FOLDERNAME = "CommandBlocks";
    private static final String EXT = ".json";

    private File dataFolder;
    private Gson gson;
    private DataFilesFilter filter;

    public JsonCommandBlockSaver(File baseFolder)
    {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.setPrettyPrinting()
                .registerTypeAdapter(CommandBlock.class, new CommandBlockGsonSerializer())
                .create();

        if (!baseFolder.exists())
        {
            baseFolder.mkdirs();
        }

        dataFolder = new File(baseFolder, FOLDERNAME);
        if (!dataFolder.exists())
        {
            dataFolder.mkdirs();
        }

        filter = new DataFilesFilter();
    }

    @Override
    public boolean save(CommandBlock cmdB)
    {
        try
        {
            File file = new File(dataFolder, cmdB.getId() + EXT);
            if (!file.exists())
            {
                file.createNewFile();
            }
            OutputStream os = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            String json = gson.toJson(cmdB);
            writer.write(json);
            writer.close();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    @Override
    public CommandBlock load(long id)
    {
        File file = new File(dataFolder, id + EXT);
        return load(file);
    }

    public CommandBlock load(File file)
    {
        if (!file.exists())
        {
            return null;
        }
        try
        {
            InputStream is = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            CommandBlock cmdB = gson.fromJson(reader, CommandBlock.class);
            reader.close();
            return cmdB;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    @Override
    public boolean saveAll(Collection<CommandBlock> cmdBlocks)
    {
        for (CommandBlock cmdBlock : cmdBlocks)
        {
            save(cmdBlock);
        }
        return true;
    }

    @Override
    public Collection<CommandBlock> loadAll()
    {
        if (!dataFolder.exists())
        {
            dataFolder.mkdirs();
            return Collections.emptyList();
        }
        List<CommandBlock> cmdBs = new LinkedList<CommandBlock>();
        File[] files = dataFolder.listFiles(filter);
        if (files != null)
        {
            for (File file : files)
            {
                CommandBlock cmd = load(file);
                if(cmd != null)
                {
                    cmdBs.add(cmd);
                }
            }
        }

        return cmdBs;
    }

    @Override
    public boolean delete(long id)
    {
        File file = new File(dataFolder, id + EXT);
        return delete(file);
    }

    public boolean delete(File file)
    {
        if (!file.exists())
        {
            return true;
        }
        file.delete();
        return true;
    }

    private static class DataFilesFilter implements FilenameFilter
    {
        private static final String ALLOWED_PATTERN = "^\\d+\\.json$";

        @Override
        public boolean accept(File dir, String name)
        {
            return name.matches(ALLOWED_PATTERN);
        }
    }
}
