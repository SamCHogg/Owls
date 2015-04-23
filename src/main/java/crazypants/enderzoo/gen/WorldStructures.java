package crazypants.enderzoo.gen;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import org.apache.commons.io.IOUtils;

import crazypants.enderzoo.Log;
import crazypants.enderzoo.gen.structure.Structure;

public class WorldStructures {

  //  private int dimensionId;

  private final Map<ChunkCoordIntPair, List<Structure>> structures = new HashMap<ChunkCoordIntPair, List<Structure>>();

  private File structFile;

  public WorldStructures(World world) {
    //    this.dimensionId = dimensionId;
    structFile = getWorldSaveFile(world);
  }

  public void add(Structure s) {
    ChunkCoordIntPair key = s.getChunkCoord();
    if(!structures.containsKey(key)) {
      structures.put(key, new ArrayList<Structure>(2));
    }
    structures.get(key).add(s);
  }

  public void addAll(Collection<Structure> structures) {
    for (Structure s : structures) {
      add(s);
    }
  }

  public List<Structure> getStructures(ChunkCoordIntPair chunkPos) {

    List<Structure> res = structures.get(chunkPos);
    if(res == null) {
      return Collections.emptyList();
    }
    return res;
  }

  public void getStructures(ChunkCoordIntPair chunkPos, String templateUid, List<Structure> result) {
    List<Structure> all = structures.get(chunkPos);
    if(all == null) {
      return;
    }
    for (Structure s : all) {
      if(templateUid == null || templateUid.equals(s.getTemplate().getUid())) {
        result.add(s);
      }
    }
  }

  public Collection<Structure> getStructures(ChunkCoordIntPair chunkPos, String templateUid) {
    List<Structure> res = new ArrayList<Structure>();
    getStructures(chunkPos, templateUid, res);
    return res;
  }

  public Collection<Structure> getStructures(int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ, String templateUid) {
    List<Structure> res = new ArrayList<Structure>();
    for (int x = minChunkX; x <= maxChunkX; x++) {
      for (int z = minChunkZ; z <= maxChunkZ; z++) {
        getStructures(new ChunkCoordIntPair(x, z), templateUid, res);
      }
    }
    return res;
  }

  public void writeToNBT(NBTTagCompound root) {
    //    root.setInteger("dimensionId", dimensionId);

    NBTTagList structTags = new NBTTagList();
    for (List<Structure> structs : structures.values()) {
      for (Structure s : structs) {
        NBTTagCompound sTag = new NBTTagCompound();
        s.writeToNBT(sTag);
        structTags.appendTag(sTag);
      }
    }
    root.setTag("structures", structTags);
  }

  void loadStructuresFromNBT(NBTTagCompound root) {

    NBTTagList structTags = (NBTTagList) root.getTag("structures");
    if(structTags != null) {
      for (int i = 0; i < structTags.tagCount(); i++) {
        NBTTagCompound structTag = structTags.getCompoundTagAt(i);
        Structure s = new Structure(structTag);
        if(s.isValid()) {
          add(s);
        } else {
          Log.warn("WorldManager: Could not load structure " + s);
        }
      }
    }

  }

  public void load() {
    if(structFile == null) {
      return;
    }
    if(!structFile.exists()) {
      return;
    }
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(structFile);
    } catch (Exception e) {
      Log.error("WorldManager: Could not open structure file for reading. File: " + structFile.getAbsolutePath());
      return;
    }

    try {
      NBTTagCompound root = CompressedStreamTools.read(new DataInputStream(fis));
      loadStructuresFromNBT(root);
    } catch (IOException e) {
      Log.error("WorldManager: Error reading structure file. File: " + structFile.getAbsolutePath() + " Exception: " + e);
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(fis);
    }
  }

  public void save() {
    if(structFile == null) {
      return;
    }
    NBTTagCompound root = new NBTTagCompound();
    writeToNBT(root);

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(structFile, false);
    } catch (FileNotFoundException e) {
      Log.error("WorldManager: could not open structure file for writing. File: " + structFile.getAbsolutePath());
      return;
    }

    try {
      CompressedStreamTools.write(root, new DataOutputStream(fos));
    } catch (IOException e) {
      Log.error("WorldManager: error writing to structure file: " + structFile.getAbsolutePath() + " Exception: " + e);
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(fos);
    }

  }

  private File getWorldSaveDir(World world) {
    File dir = new File(world.getSaveHandler().getWorldDirectory(), "enderzoo");
    if(!dir.exists()) {
      if(!dir.mkdir()) {
        Log.error("WorldManager: Could not create structures directory: " + dir.getAbsolutePath());
      }
    }
    return dir;
  }

  private File getWorldSaveFile(World world) {
    File res = new File(getWorldSaveDir(world), "structures_dim" + world.provider.dimensionId + ".nbt");
    return res;
  }

}