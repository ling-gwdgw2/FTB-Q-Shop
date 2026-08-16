import os
import subprocess
import glob
import zipfile
import shutil

project_root = os.path.dirname(os.path.abspath(__file__))
build_dir = os.path.join(project_root, "build")
libs_out_dir = os.path.join(build_dir, "libs")
out_classes = os.path.join(build_dir, "classes")
out_jar = os.path.join(libs_out_dir, "ling_q_shop-v1.3-neoforge-1.21.1.jar")
root_jar = os.path.join(project_root, "ling_q_shop-v1.3-neoforge-1.21.1.jar")

if os.path.exists(out_classes):
    shutil.rmtree(out_classes)
os.makedirs(out_classes, exist_ok=True)
os.makedirs(libs_out_dir, exist_ok=True)

if os.path.exists(out_jar):
    os.remove(out_jar)
if os.path.exists(root_jar):
    os.remove(root_jar)

# 1. Collect CP with valid zipfile check
def is_valid_jar(p):
    if not p.endswith(".jar"): return False
    try:
        if os.path.getsize(p) < 100: return False
        with zipfile.ZipFile(p, 'r') as z:
            return z.testzip() is None
    except Exception:
        return False

user_home = os.path.expanduser("~")
gradle_cache = os.path.join(user_home, ".gradle", "caches")
curseforge_libs = os.path.join(user_home, "curseforge", "minecraft", "Install", "libraries")

cp_jars = []

# Priority 1.21.1 NeoForge and vanilla client jars
priority_jars = [
    os.path.join(curseforge_libs, "net", "neoforged", "neoforge", "21.1.244", "neoforge-21.1.244-client.jar"),
    os.path.join(curseforge_libs, "net", "neoforged", "neoforge", "21.1.244", "neoforge-21.1.244-universal.jar"),
    os.path.join(curseforge_libs, "net", "minecraft", "client", "1.21.1-20240808.144430", "client-1.21.1-20240808.144430-extra.jar"),
    os.path.join(curseforge_libs, "net", "minecraft", "client", "1.21.1-20240808.144430", "client-1.21.1-20240808.144430-srg.jar"),
    os.path.join(curseforge_libs, "net", "minecraft", "client", "1.21.1", "client-1.21.1-official.jar")
]

for pj in priority_jars:
    if os.path.exists(pj) and is_valid_jar(pj):
        cp_jars.append(pj.replace("\\", "/"))

# Scan libraries excluding old forge jars
if os.path.exists(curseforge_libs):
    for root, _, files in os.walk(curseforge_libs):
        for f in files:
            if f.endswith(".jar") and "minecraftforge" not in root.lower():
                full = os.path.join(root, f)
                if is_valid_jar(full) and full.replace("\\", "/") not in cp_jars:
                    cp_jars.append(full.replace("\\", "/"))

# Scan gradle caches for mod dependencies (FTB, etc.)
if os.path.exists(gradle_cache):
    for root, _, files in os.walk(os.path.join(gradle_cache, "modules-2")):
        for f in files:
            if f.endswith(".jar") and not f.endswith("-sources.jar") and not f.endswith("-javadoc.jar"):
                if "ftb" in f.lower() or "architectury" in f.lower():
                    full = os.path.join(root, f)
                    if is_valid_jar(full) and full.replace("\\", "/") not in cp_jars:
                        cp_jars.append(full.replace("\\", "/") )

# Scan NeoForge 1.21.1 instances if available
nf_instance_mods = os.path.join(user_home, "curseforge", "minecraft", "Instances", "ling_q_shop-v1.3-neoforge", "mods")
if os.path.exists(nf_instance_mods):
    for f in os.listdir(nf_instance_mods):
        if f.endswith(".jar") and not f.startswith("ling_q_shop"):
            full = os.path.join(nf_instance_mods, f)
            if is_valid_jar(full) and full.replace("\\", "/") not in cp_jars:
                cp_jars.append(full.replace("\\", "/"))

# Local libs folder if available
local_libs = os.path.join(project_root, "libs")
if os.path.exists(local_libs):
    for f in os.listdir(local_libs):
        full = os.path.join(local_libs, f)
        if is_valid_jar(full) and full.replace("\\", "/") not in cp_jars:
            cp_jars.append(full.replace("\\", "/"))

classpath = ";".join(cp_jars)

# 2. Collect Java files
java_files = []
for root, _, files in os.walk(os.path.join(project_root, "com")):
    for f in files:
        if f.endswith(".java"):
            java_files.append(os.path.join(root, f).replace("\\", "/"))

print(f"Compiling {len(java_files)} Java files for Java 21 with {len(cp_jars)} valid classpath JARs...")

args_txt = os.path.join(project_root, "args.txt")
with open(args_txt, "w", encoding="utf-8") as f:
    f.write("-encoding\nUTF-8\n")
    f.write("-source\n21\n")
    f.write("-target\n21\n")
    f.write("-proc:none\n")
    f.write("-sourcepath\n")
    f.write(f'"{project_root.replace(os.sep, "/")}"\n')
    f.write("-d\n")
    f.write(f'"{out_classes.replace(os.sep, "/")}"\n')
    f.write("-cp\n")
    f.write(f'"{classpath};{project_root.replace(os.sep, "/")}"\n')
    for jf in java_files:
        f.write(f'"{jf}"\n')

# 3. Invoke Javac using argfile
javac_cmd = [
    "javac",
    "@" + args_txt.replace("\\", "/")
]

res = subprocess.run(javac_cmd, capture_output=True, text=True)
if res.stdout:
    print("STDOUT:", res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)

if os.path.exists(args_txt):
    os.remove(args_txt)

if res.returncode != 0:
    print(f"Compilation failed with exit code {res.returncode}")
    exit(1)

print("Compilation successful!")

# 4. Packaging JAR
with zipfile.ZipFile(out_jar, "w", zipfile.ZIP_DEFLATED) as z:
    # Add classes
    for root, _, files in os.walk(out_classes):
        for f in files:
            full = os.path.join(root, f)
            rel = os.path.relpath(full, out_classes)
            z.write(full, rel)
    # Add META-INF
    meta_dir = os.path.join(project_root, "META-INF")
    if os.path.exists(meta_dir):
        for f in os.listdir(meta_dir):
            full = os.path.join(meta_dir, f)
            z.write(full, os.path.join("META-INF", f))
    # Add assets
    assets_dir = os.path.join(project_root, "assets")
    if os.path.exists(assets_dir):
        for root, _, files in os.walk(assets_dir):
            for f in files:
                full = os.path.join(root, f)
                rel = os.path.relpath(full, project_root)
                z.write(full, rel)
    # Add data
    data_dir = os.path.join(project_root, "data")
    if os.path.exists(data_dir):
        for root, _, files in os.walk(data_dir):
            for f in files:
                full = os.path.join(root, f)
                rel = os.path.relpath(full, project_root)
                z.write(full, rel)
    # Add pack.mcmeta
    pack_mc = os.path.join(project_root, "pack.mcmeta")
    if os.path.exists(pack_mc):
        z.write(pack_mc, "pack.mcmeta")
    # Add logo.png
    logo_file = os.path.join(project_root, "logo.png")
    if os.path.exists(logo_file):
        z.write(logo_file, "logo.png")

# Copy to root jar for easy release
shutil.copyfile(out_jar, root_jar)
print(f"Build complete! Output JAR: {out_jar}")
print(f"Release JAR: {root_jar}")
