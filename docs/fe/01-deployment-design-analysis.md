# Deployment Design Analysis — NexGen Credit Risk Gateway

---

| **Field** | **Value** |
|---|---|
| **Project** | NexGen Credit Risk Gateway — Deployment |
| **Application** | Spring Boot 3.3.5 / Java 21 / CXF 4.0.5 / MongoDB |
| **Date** | 01-Apr-2026 |
| **Status** | Draft — Awaiting Decision |
| **Audience** | DevOps Beginner |

---

## 1. Executive Summary

This document analyzes deployment approaches for the NexGen Credit Risk Gateway Spring Boot application. It addresses four key questions:

1. **Can we deploy on GitHub only?** — No, not for production. GitHub hosts code and runs CI/CD pipelines, but cannot run a persistent Spring Boot JVM process.
2. **Can GitHub help deploy to AWS?** — Yes! GitHub Actions is the bridge. It builds your code and pushes it to AWS automatically.
3. **Free-Trial AWS constraints** — Fully feasible with careful resource planning (t2.micro + MongoDB Atlas free tier).
4. **Skip testing?** — Feasible for initial deployment, but with documented trade-offs.

**Recommended approach**: GitHub Actions CI/CD → AWS EC2 (Free Tier) with MongoDB Atlas Free Tier.

---

## 2. Deployment Concepts for Beginners

Before diving into experiments, here's a quick primer on the key concepts:

### 2.1 What is CI/CD?

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        CI/CD Pipeline                                    │
│                                                                          │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────────────┐   │
│  │  COMMIT   │───→│  BUILD   │───→│  TEST    │───→│  DEPLOY to AWS   │   │
│  │ (GitHub)  │    │ (Maven)  │    │ (JUnit)  │    │ (EC2 instance)   │   │
│  └──────────┘    └──────────┘    └──────────┘    └──────────────────┘   │
│       ↑               ↑               ↑                ↑                 │
│  You push code   GitHub Actions   GitHub Actions   GitHub Actions        │
│  to GitHub       compiles JAR     runs tests       SSHes to EC2          │
│                                                    & deploys JAR         │
└──────────────────────────────────────────────────────────────────────────┘
```

| Term | What it means |
|---|---|
| **CI** (Continuous Integration) | Automatically build + test your code on every push |
| **CD** (Continuous Deployment) | Automatically deploy to a server after CI passes |
| **GitHub Actions** | GitHub's free CI/CD automation engine (runs in GitHub's cloud) |
| **EC2** | A virtual server (computer) in AWS cloud that you rent |
| **IAM** | AWS Identity & Access Management — controls who can access what |
| **Security Group** | AWS firewall rules — controls which ports are open |
| **systemd** | Linux service manager — keeps your app running, auto-restarts on crash |
| **SSH** | Secure Shell — how you (and GitHub Actions) connect to your EC2 remotely |

### 2.2 How the Pieces Fit Together

```
┌─────────────────────┐         ┌─────────────────────────────────────┐
│     YOUR LAPTOP     │         │            GITHUB                    │
│                     │         │                                      │
│  Code Editor (VS    │  push   │  Repository (stores your code)       │
│  Code + Copilot)    │───────→ │         ↓                            │
│                     │         │  GitHub Actions (builds & deploys)   │
└─────────────────────┘         │         ↓                            │
                                │  Uploads JAR to EC2 via SSH          │
                                └──────────┬──────────────────────────┘
                                           │
                                           ↓ SSH + SCP
                        ┌──────────────────────────────────────┐
                        │          AWS EC2 (Free Tier)          │
                        │                                       │
                        │  Java 21 runtime                      │
                        │  nexgen-creditrisk-gateway.jar         │
                        │  systemd service (auto-restart)       │
                        │  Port 8080 → your application         │
                        └──────────────────┬───────────────────┘
                                           │
                                           ↓ network
                        ┌──────────────────────────────────────┐
                        │     MongoDB Atlas (Free Tier)         │
                        │  512 MB storage, hosted cloud DB      │
                        └──────────────────────────────────────┘
```

---

## 3. Question Analysis

### 3.1 — Can We Deploy on GitHub Only?

**Short answer: No**, not for a Spring Boot application in production.

| GitHub Service | Can it host our app? | Why / Why not |
|---|---|---|
| **GitHub Pages** | ❌ No | Only serves static HTML/CSS/JS files. Cannot run Java/JVM. |
| **GitHub Packages** | ❌ No | Stores build artifacts (JARs, Docker images). Not a runtime. |
| **GitHub Codespaces** | ⚠️ Dev only | Cloud dev environment. Can run the app temporarily but it shuts down when idle. Not meant for production hosting. |
| **GitHub Actions** | ⚠️ Build only | Runs your CI/CD pipeline (up to 6 hrs per job). Cannot host a persistent service. |

**Why GitHub can't host our app:**
Our application is a **persistent JVM process** that listens on port 8080 for SOAP/REST requests and needs a MongoDB connection. GitHub doesn't offer always-on compute — it offers code hosting, automation, and artifact storage. You need a **server** (like AWS EC2) to actually run the application.

**What GitHub IS perfect for:**
- ✅ Storing your code (repository)
- ✅ Building your JAR automatically (GitHub Actions CI)
- ✅ Deploying to AWS automatically (GitHub Actions CD)
- ✅ Monitoring build status
- ✅ Storing build artifacts temporarily

### 3.2 — Can GitHub Help Deploy to AWS?

**Short answer: Yes, absolutely!** This is the recommended approach.

GitHub Actions acts as the **bridge** between your code and AWS:

```
 git push → GitHub Actions → Build JAR → SSH to EC2 → Deploy JAR → Health Check
```

**How it works:**
1. You push code to the `main` branch
2. GitHub Actions automatically triggers
3. It compiles your code and creates the JAR file
4. It securely connects to your EC2 instance via SSH
5. It copies the JAR to EC2 and runs the deploy script
6. It verifies the app is healthy

**We already have most of this built!** Our existing `ci.yml` handles steps 1-3. We just need to add steps 4-6 (the CD part).

### 3.3 — Free-Trial AWS Constraints

AWS Free Tier (12 months from account creation) gives us:

| Service | Free Allowance | Our Usage | Feasible? |
|---|---|---|---|
| **EC2 t2.micro** | 750 hrs/month (1 vCPU, 1 GB RAM) | Run 24/7 = ~730 hrs | ✅ Yes |
| **EBS (disk)** | 30 GB General Purpose SSD | ~500 MB for OS + JDK + JAR | ✅ Yes |
| **Data Transfer** | 15 GB/month outbound | Minimal for dev/demo | ✅ Yes |
| **S3** | 5 GB storage | Not needed initially | ✅ N/A |
| **RDS** | 750 hrs db.t2.micro | Not using (using MongoDB Atlas) | ✅ N/A |
| **MongoDB Atlas** | 512 MB free forever | Transaction logs, lightweight | ✅ Yes |

**⚠️ Critical constraint — Memory:**
- t2.micro has only **1 GB RAM** total
- Linux OS uses ~200-300 MB
- Java 21 + Spring Boot needs ~400-512 MB
- That leaves **very little headroom**

**Solution:** Use MongoDB Atlas (cloud-hosted, free tier) instead of running MongoDB on the same EC2 instance. This saves ~300 MB RAM.

**💰 Estimated monthly cost: $0.00** (if staying within Free Tier limits)

**⚠️ What to avoid (costs money on Free Tier):**
- Do NOT create a NAT Gateway ($0.045/hr = ~$32/month)
- Do NOT use Elastic IP without attaching it to a running instance ($0.005/hr)
- Do NOT leave stopped instances with EBS volumes beyond 30 GB
- Do NOT create an Application Load Balancer ($0.0225/hr = ~$16/month)
- Do NOT use RDS (we're using MongoDB Atlas instead)

### 3.4 — Can We Skip Testing and Start Deployment?

**Short answer: Yes, it's feasible.** Skipping tests will NOT prevent deployment. But understand the trade-offs:

| Aspect | With Tests | Without Tests |
|---|---|---|
| **Deployment possible?** | ✅ Yes | ✅ Yes |
| **Build command** | `mvn clean verify` (compile + test + package) | `mvn clean package -DskipTests` (compile + package) |
| **Confidence in code** | High — tests catch bugs before deploy | Lower — bugs discovered on the running server |
| **Debugging effort** | Bugs caught early, clear test failure messages | Bugs found at runtime, harder to diagnose on EC2 |
| **CI pipeline speed** | Slower (runs tests) | Faster |
| **Professional practice** | ✅ Industry standard | ⚠️ Acceptable for prototypes |

**Recommendation: Skip tests for now, but with safeguards:**

1. ✅ **Ensure `mvn clean package -DskipTests` succeeds** — this confirms compilation is clean
2. ✅ Modify CI pipeline to skip tests temporarily using a flag
3. ✅ Use `stub-enabled: true` for bureau service (avoids external dependency failures at runtime)
4. ✅ Keep Actuator health endpoint enabled (gives you runtime health status)
5. 📋 Plan to add tests back in a future iteration

**Impact on deployment: NONE.** The deployment process doesn't care whether tests ran — it only needs a valid JAR file.

---

## 4. Deployment Approach Experiments

### EXP-D01: Deployment Target — EC2 vs Elastic Beanstalk vs ECS

| Attribute | Details |
|---|---|
| **Hypothesis** | EC2 with a simple deploy script is the best fit for a beginner on Free Tier with a single Spring Boot JAR. |

| Approach | Description | Free Tier? | Complexity | Best For |
|---|---|---|---|---|
| **A: EC2 (manual)** | Launch a Linux VM, install Java, SCP the JAR, run with systemd | ✅ Yes (t2.micro) | Low-Medium | Our case — single app, learning DevOps |
| **B: Elastic Beanstalk** | AWS managed platform — upload JAR, AWS handles the rest | ⚠️ Partially (uses EC2 + ELB underneath, ELB costs ~$16/mo) | Low | Teams who don't want to manage infra |
| **C: ECS Fargate** | Containerized deployment (Docker required) | ❌ No free tier for Fargate | High | Microservices, auto-scaling |
| **D: AWS Lambda** | Serverless functions | ❌ Not suitable | Medium | Event-driven, NOT for persistent SOAP endpoints |
| **E: Lightsail** | Simplified VPS | ⚠️ $3.50/month minimum | Lowest | Simple websites |

**Decision: Approach A (EC2)** — Free Tier eligible, teaches real DevOps skills, we already have deploy scripts.

Why not Elastic Beanstalk? It automatically creates an Elastic Load Balancer which costs ~$16/month (not free). For a single instance, EC2 is simpler and free.

### EXP-D02: Database — MongoDB on EC2 vs MongoDB Atlas Free Tier

| Attribute | Details |
|---|---|
| **Hypothesis** | MongoDB Atlas Free Tier is better for our t2.micro constraint because it offloads memory usage to a managed service. |

| Approach | Pros | Cons |
|---|---|---|
| **A: MongoDB on same EC2** | No external dependency, full control | Uses ~300 MB RAM (t2.micro only has 1 GB total), manual backup, manual security |
| **B: MongoDB Atlas Free Tier** | 512 MB storage free forever, managed backups, UI dashboard, saves EC2 RAM | External dependency, network latency (~1-5ms), 512 MB limit |

**Decision: Approach B (MongoDB Atlas Free Tier)**

Rationale: With only 1 GB RAM on t2.micro, running both Java (512 MB) and MongoDB (300 MB) on the same instance would leave zero headroom. Atlas frees up that RAM and provides a managed experience.

Atlas Free Tier specs:
- 512 MB storage (more than enough for transaction logs)
- Shared M0 cluster
- Available in AWS regions (choose same region as EC2 for low latency)
- Free forever (not limited to 12 months)

### EXP-D03: CI/CD Pipeline — GitHub Actions vs AWS CodePipeline vs Jenkins

| Attribute | Details |
|---|---|
| **Hypothesis** | GitHub Actions is the simplest CI/CD choice since our code already lives in GitHub and we already have a CI workflow. |

| Approach | Free Tier? | Complexity | Integration |
|---|---|---|---|
| **A: GitHub Actions** | ✅ 2,000 min/month free | Low | Native GitHub, SSH to EC2 |
| **B: AWS CodePipeline** | ⚠️ 1 free pipeline/month | Medium | Native AWS, needs S3 + CodeDeploy |
| **C: Jenkins** | ❌ Self-hosted (needs server) | High | Universal, needs maintenance |

**Decision: Approach A (GitHub Actions)** — Already set up, free, native integration with our repo.

### EXP-D04: Java Runtime — Amazon Corretto vs Eclipse Temurin vs Oracle JDK

| Attribute | Details |
|---|---|
| **Hypothesis** | Amazon Corretto 21 is the best choice for EC2 since it's maintained by AWS, optimized for AWS, and free. |

| Approach | Vendor | EC2 Optimized? | Long-Term Support? |
|---|---|---|---|
| **A: Amazon Corretto 21** | Amazon | ✅ Yes | ✅ Free LTS |
| **B: Eclipse Temurin 21** | Adoptium | No | ✅ Free LTS |
| **C: Oracle JDK 21** | Oracle | No | ⚠️ License restrictions |

**Decision: Approach A (Amazon Corretto 21)** — Maintained by AWS, available in yum/apt repos on Amazon Linux, free.

### EXP-D05: OS Image — Amazon Linux 2023 vs Ubuntu 22.04

| Attribute | Details |
|---|---|
| **Hypothesis** | Amazon Linux 2023 is the simplest choice for EC2 since it's maintained by AWS and optimized for the platform. |

| Approach | Pros | Cons |
|---|---|---|
| **A: Amazon Linux 2023** | AWS-optimized, Corretto in default repos, smaller footprint | Less community tutorials |
| **B: Ubuntu 22.04 LTS** | Huge community, familiar `apt` package manager | Slightly larger base image |

**Decision: Approach B (Ubuntu 22.04 LTS)** — Better for beginners. More stackoverflow answers, tutorials, and community support. Familiar to most developers.

---

## 5. Recommended Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                      GITHUB (Free)                              │
│                                                                  │
│  Repository: yash-bharadwaj-cof/nexgen-creditrisk-gateway       │
│                                                                  │
│  GitHub Actions CI/CD Pipeline:                                  │
│  ┌────────┐ ┌─────────┐ ┌──────────────┐ ┌──────────────────┐  │
│  │Checkout│→│Build JAR│→│Upload to EC2 │→│Run deploy.sh     │  │
│  │  code  │ │(Maven)  │ │  (via SSH)   │ │(restart service) │  │
│  └────────┘ └─────────┘ └──────────────┘ └──────────────────┘  │
└────────────────────────────────────────────────────────────────┘
                              │
                              ↓ SSH (port 22)
┌────────────────────────────────────────────────────────────────┐
│                 AWS EC2 — Free Tier                              │
│                                                                  │
│  Instance: t2.micro (1 vCPU, 1 GB RAM)                         │
│  OS: Ubuntu 22.04 LTS                                           │
│  JDK: Amazon Corretto 21                                        │
│                                                                  │
│  ┌──────────────────────────────────────────────────────┐       │
│  │            nexgen-creditrisk-gateway.jar               │       │
│  │            (Spring Boot 3.3.5, port 8080)              │       │
│  │                                                        │       │
│  │  REST: GET /nexgen/api/v1/creditrisk/assess           │       │
│  │  SOAP: POST /nexgen/services/CreditRiskService        │       │
│  │  Health: GET /nexgen/actuator/health                   │       │
│  └──────────────────────────────────────────────────────┘       │
│                                                                  │
│  Security Group (firewall):                                      │
│    - Port 22 (SSH) ← your IP only                               │
│    - Port 8080 (App) ← 0.0.0.0/0 (or your IP only)            │
│                                                                  │
│  Disk: 8 GB EBS gp3 (within 30 GB free allowance)              │
└────────────────────────────────────────────────────────────────┘
                              │
                              ↓ MongoDB wire protocol (port 27017)
┌────────────────────────────────────────────────────────────────┐
│              MongoDB Atlas — Free Tier (M0)                      │
│                                                                  │
│  Cluster: nexgen-cluster (same AWS region as EC2)               │
│  Database: nexgen_creditrisk                                     │
│  Collection: transactions                                        │
│  Storage: 512 MB (free forever)                                  │
│  Connection: mongodb+srv://...                                   │
└────────────────────────────────────────────────────────────────┘
```

---

## 6. Technology Decision Summary

| Decision | Selected | Rationale |
|---|---|---|
| **Compute** | AWS EC2 t2.micro | Free Tier, teaches real ops, scripts already exist |
| **OS** | Ubuntu 22.04 LTS | Beginner-friendly, huge community |
| **JDK** | Amazon Corretto 21 | AWS-optimized, free, LTS |
| **Database** | MongoDB Atlas Free (M0) | Saves EC2 RAM, managed, free forever |
| **CI/CD** | GitHub Actions | Already set up, free 2,000 min/month |
| **Deployment** | SSH + deploy.sh (systemd) | Simple, deploy script already exists |
| **JVM Settings** | `-Xms256m -Xmx512m` | Fits in t2.micro with ~300 MB headroom |
| **Spring Profile** | `prod` | Production logging, security settings |
| **Monitoring** | Spring Actuator `/health` | Already configured, zero cost |
| **Testing** | Skip initially (`-DskipTests`) | Unblocks deployment, add tests later |

---

## 7. Implementation Roadmap

### Phase 1 — AWS Account Setup (One-time, ~30 minutes)

| Step | Action | Details |
|---|---|---|
| 1.1 | Sign into AWS Console | https://console.aws.amazon.com |
| 1.2 | Set your region | Choose `us-east-1` (most free-tier services available) |
| 1.3 | Create IAM user | Create a non-root user with `AdministratorAccess` for daily use |
| 1.4 | Enable MFA | Add multi-factor authentication to both root and IAM user |
| 1.5 | Set billing alert | Budget → $1 alert (catches accidental charges immediately) |

### Phase 2 — MongoDB Atlas Setup (One-time, ~15 minutes)

| Step | Action | Details |
|---|---|---|
| 2.1 | Sign up at mongodb.com/atlas | Free account, no credit card needed |
| 2.2 | Create Free M0 cluster | Select AWS, same region as EC2 (us-east-1) |
| 2.3 | Create database user | Username/password for app connection |
| 2.4 | Whitelist EC2 IP | Or use `0.0.0.0/0` for dev (restrict later) |
| 2.5 | Get connection string | `mongodb+srv://<user>:<pass>@cluster0.xxxxx.mongodb.net/nexgen_creditrisk` |

### Phase 3 — EC2 Instance Setup (One-time, ~30 minutes)

| Step | Action | Details |
|---|---|---|
| 3.1 | Launch EC2 instance | Ubuntu 22.04 LTS, t2.micro, 8 GB gp3 |
| 3.2 | Create key pair | Download `.pem` file — this is your SSH key (KEEP IT SAFE) |
| 3.3 | Configure Security Group | Port 22 (SSH, your IP), Port 8080 (App, your IP or 0.0.0.0/0) |
| 3.4 | SSH into instance | `ssh -i key.pem ubuntu@<ec2-public-ip>` |
| 3.5 | Install Corretto 21 | `sudo apt update && sudo apt install -y java-21-amazon-corretto-jdk` |
| 3.6 | Create app user | `sudo useradd -r -s /bin/false nexgen` |
| 3.7 | Create app directory | `sudo mkdir -p /opt/nexgen/creditrisk && sudo chown nexgen:nexgen /opt/nexgen/creditrisk` |
| 3.8 | Upload env.conf | Fill in MongoDB Atlas connection string and credentials |
| 3.9 | Install systemd service | Copy `nexgen-creditrisk.service` to `/etc/systemd/system/` |
| 3.10 | Enable service | `sudo systemctl daemon-reload && sudo systemctl enable nexgen-creditrisk` |

### Phase 4 — GitHub Actions CD Pipeline (One-time, ~20 minutes)

| Step | Action | Details |
|---|---|---|
| 4.1 | Add GitHub Secrets | `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY` (the .pem contents) |
| 4.2 | Update CI/CD workflow | Add deployment job that SSHes to EC2 after build |
| 4.3 | Test with a push | Push to main and watch the Actions tab |

### Phase 5 — Verify & Harden (~15 minutes)

| Step | Action | Details |
|---|---|---|
| 5.1 | Check health endpoint | `curl http://<ec2-ip>:8080/nexgen/actuator/health` |
| 5.2 | Test REST endpoint | `curl http://<ec2-ip>:8080/nexgen/api/v1/creditrisk/assess?...` |
| 5.3 | Check logs | `sudo journalctl -u nexgen-creditrisk -f` |
| 5.4 | Restrict Security Group | Lock SSH to your IP only if not already done |

---

## 8. JVM Tuning for t2.micro (1 GB RAM)

The default JVM settings in `env.conf.template` specify `-Xms512m -Xmx1024m`, which will NOT fit in t2.micro. Updated settings:

```bash
# Tuned for t2.micro (1 GB RAM total)
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m
```

**Memory budget:**
| Component | Allocation |
|---|---|
| Linux OS + system processes | ~250 MB |
| JVM Heap (`-Xmx512m`) | 512 MB |
| JVM Metaspace + overhead | ~150 MB |
| **Remaining buffer** | ~112 MB |
| **Total** | 1024 MB |

This is tight but workable for a dev/demo deployment.

---

## 9. GitHub Actions CD Workflow (Preview)

Here's what the updated CI/CD pipeline will look like:

```yaml
# .github/workflows/ci.yml (updated with CD)
name: CI/CD — NexGen Credit Risk Gateway

on:
  push:
    branches: [main]
    paths: ['forward-engineering/**']

jobs:
  build:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: forward-engineering
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      - name: Build JAR (skip tests for now)
        run: mvn clean package -DskipTests -B
      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: nexgen-creditrisk-gateway
          path: forward-engineering/target/*.jar

  deploy:
    needs: build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/download-artifact@v4
        with:
          name: nexgen-creditrisk-gateway
          path: ./artifact
      - name: Deploy to EC2
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            # The JAR will be copied separately via SCP
            echo "Deployment triggered"
      - name: Copy JAR to EC2
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          source: "artifact/*.jar"
          target: "/tmp/nexgen-deploy"
          strip_components: 1
      - name: Run deploy script
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            cd /tmp/nexgen-deploy
            JAR_FILE=$(ls *.jar | head -1)
            sudo /opt/nexgen/creditrisk/deploy.sh "/tmp/nexgen-deploy/${JAR_FILE}"
            rm -rf /tmp/nexgen-deploy
```

> **Note:** This is a preview. The actual workflow will be created when we proceed with implementation.

---

## 10. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| **t2.micro runs out of memory** | Medium | App crashes / OOM | Tune JVM to 512m max, use Atlas for MongoDB, monitor with `free -m` |
| **Free Tier expires (12 months)** | Certain | ~$8-10/month charges | Set billing alerts, decide to keep or teardown before expiry |
| **Deploying buggy code (no tests)** | Medium | Runtime errors | Ensure `mvn package` compiles cleanly, use health endpoint, check logs |
| **SSH key leaked** | Low | Full server access from anywhere | Store as GitHub Secret (encrypted), never commit to repo |
| **Bureau service fails at runtime** | N/A | Service degradation | `stub-enabled: true` by default — no real bureau dependency |
| **MongoDB Atlas connection issues** | Low | Logs not persisted | App still processes requests, just skips logging (async fire-and-forget) |
| **Exceeding free tier limits** | Low | Unexpected charges | Set $1 billing alert, single t2.micro runs 24/7 within limit |

---

## 11. What We Already Have vs What We Need

| Component | Status | Action Needed |
|---|---|---|
| Source code (Spring Boot app) | ✅ Complete | None |
| `mvn clean compile` | ✅ Passes | Change to `mvn clean package -DskipTests` |
| `forward-engineering/ec2/deploy.sh` | ✅ Exists | Minor update for JVM opts |
| `forward-engineering/ec2/env.conf.template` | ✅ Exists | Update JVM opts and MongoDB URI format |
| `forward-engineering/ec2/nexgen-creditrisk.service` | ✅ Exists | No changes needed |
| `.github/workflows/ci.yml` | ✅ Exists (CI only) | **ADD: CD deploy job** |
| AWS EC2 instance | ❌ Not created | **CREATE: Phase 3 of roadmap** |
| MongoDB Atlas cluster | ❌ Not created | **CREATE: Phase 2 of roadmap** |
| GitHub Secrets (SSH key, EC2 IP) | ❌ Not configured | **ADD: Phase 4 of roadmap** |
| IAM user + billing alerts | ❌ Not configured | **CREATE: Phase 1 of roadmap** |

---

## 12. Questions for You

Before we proceed with implementation, I'd like to confirm a few things:

1. **AWS Region preference** — Do you have a preference, or shall we use `us-east-1` (most free-tier friendly)?

2. **MongoDB Atlas** — Are you comfortable creating a free MongoDB Atlas account (at mongodb.com/atlas), or would you prefer to try running MongoDB on the same EC2 instance (risky with 1 GB RAM)?

3. **Domain name** — Will you access the app via the EC2 public IP address (e.g., `http://54.12.34.56:8080`), or do you have a domain name you'd like to use?

4. **Security level** — For this dev/demo deployment, is it acceptable to:
   - Open port 8080 to the internet (anyone can access the API)?
   - Or should we restrict it to your IP address only?

5. **Deployment trigger** — Should deployment happen:
   - Automatically on every push to `main`? (recommended)
   - Or manually via a GitHub Actions workflow dispatch button?

6. **Next step** — Would you like me to:
   - **Option A**: Start with the AWS setup guide (step-by-step EC2 + Atlas creation)?
   - **Option B**: Update the CI/CD pipeline first (add the deploy job to `ci.yml`)?
   - **Option C**: Both — full end-to-end implementation?

---

## Appendix A: Cost Estimator

| Scenario | Monthly Cost | Notes |
|---|---|---|
| **Within Free Tier (first 12 months)** | $0.00 | t2.micro 24/7 + Atlas M0 |
| **After Free Tier expires** | ~$8.50/month | t2.micro on-demand ($0.0116/hr) |
| **If you accidentally create ELB** | +$16.20/month | Delete it immediately! |
| **If you forget to stop instance** | $0.00 (within free tier) | But EBS charges after 30 GB |

## Appendix B: Useful Commands (Cheat Sheet)

```bash
# SSH into your EC2
ssh -i ~/nexgen-key.pem ubuntu@<ec2-public-ip>

# Check if app is running
sudo systemctl status nexgen-creditrisk

# View live logs
sudo journalctl -u nexgen-creditrisk -f

# Check memory usage
free -m

# Restart the app
sudo systemctl restart nexgen-creditrisk

# Check which ports are in use
sudo ss -tlnp

# Test the health endpoint
curl http://localhost:8080/nexgen/actuator/health

# Manual deploy (if not using CI/CD)
sudo /opt/nexgen/creditrisk/deploy.sh /path/to/your.jar
```

---

*Document Version: 1.0 — Created 01-Apr-2026*
