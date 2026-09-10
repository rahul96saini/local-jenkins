# Jenkins User Authentication & Authorization Assignment

## Table of Contents

1.  [Authorization Strategies](#step-1-4-authorization-strategies)
2.  [Why We Choose Role-Based
    Strategy](#why-we-choose-role-based-strategy)
3.  [Install the Role-Based Authorization Strategy
    Plugin](#step-2-install-the-plugin)
4.  [Enable Authentication (Security
    Realm)](#step-3-enable-authentication-security-realm)
5.  [Set Authorization Strategy to
    Role-Based](#step-4-set-authorization-strategy-to-role-based)
6.  [Create the 9 Jobs](#step-5-create-the-9-jobs)
7.  [Create the 3 Views](#step-6-create-the-3-views)
8.  [Configure
    Roles](#step-7-configure-roles-the-core-of-the-assignment)
    -   [Global
        Roles](#a-global-roles-controls-login--basic-jenkins-access)
    -   [Item Roles](#b-item-roles-job-level-pattern-based)
    -   [Assign Roles to Users](#c-assign-roles-to-users)
9.  [Verify](#step-8-verify)

------------------------------------------------------------------------

# Step 1: 4 Authorization Strategies

Jenkins provides different authorization strategies for controlling what
users are allowed to access and do.

The four authorization strategies covered in this assignment are:

1.  **Legacy mode**
2.  **Project-based Matrix Authorization**
3.  **Matrix-based (global only)**
4.  **Role-Based Strategy**
    -   Requires the **Role-based Authorization Strategy** plugin.

------------------------------------------------------------------------

# Why We Choose Role-Based Strategy

The requirement is essentially:

> **"Team X can access all jobs matching a naming pattern."**

The job naming patterns are:

-   `dev-*`
-   `test-*`
-   `devops-*`

The **Role-Based Authorization Strategy** is well suited for this
requirement because its **Item Roles** support regular-expression-based
job matching.

This allows access to be organized using rules based on job names.

For example:

``` text
Developer Team → dev-.*
Testing Team   → dev-.* | test-.*
DevOps Team    → dev-.* | test-.* | devops-.*
```

Role-Based Strategy can therefore express the required job-level access
using rules for each team.

------------------------------------------------------------------------

# Step 2: Install the Plugin

Go to:

**Manage Jenkins → Plugins → Available plugins**

Search for:

``` text
Role-based Authorization Strategy
```

Install the plugin.

Restart Jenkins if prompted.

------------------------------------------------------------------------

# Step 3: Enable Authentication (Security Realm)

Go to:

**Manage Jenkins → Security → Security Realm**

Select:

**Jenkins' own user database**

Uncheck:

**Allow users to sign up**

This ensures that users will be created manually by the administrator.

Click **Save**.

## Create the 7 Users

Go to:

**Manage Jenkins → Users → Create User**

Create the following users:

### Developer

-   `developer-1`
-   `developer-2`

### Testing

-   `testing-1`
-   `testing-2`

### DevOps

-   `devops-1`
-   `devops-2`

### Administration

-   `admin-1`

Give each user a password and keep a record of the credentials.

------------------------------------------------------------------------

# Step 4: Set Authorization Strategy to Role-Based

Go to:

**Manage Jenkins → Security → Authorization**

Select:

**Role-Based Strategy**

Click **Save**.

> **Important safety step:** Before saving, make sure your own
> account and `admin-1` will receive appropriate administrative rights
> in the very next step. Otherwise, you may lock yourself out of
> Jenkins.

------------------------------------------------------------------------

# Step 5: Create the 9 Jobs

For each job, use:

**New Item → Freestyle project**

Then, under:

**Build Steps → Execute shell**

use the following on Linux/macOS:

``` bash
echo "Job Name: $JOB_NAME"
echo "Build Number: $BUILD_NUMBER"
```

If you are using Windows, select **Execute Windows batch command** and
use:

``` bat
echo Job Name: %JOB_NAME%
echo Build Number: %BUILD_NUMBER%
```

## Jobs to Create

### Developer Jobs

-   `dev-1`
-   `dev-2`
-   `dev-3`

### Testing Jobs

-   `test-1`
-   `test-2`
-   `test-3`

### DevOps Jobs

-   `devops-1`
-   `devops-2`
-   `devops-3`

Use the same build step for all 9 jobs.

Run each job once and verify that the console output displays the job
name and build number.

Example:

``` text
Job Name: dev-1
Build Number: 1
```

------------------------------------------------------------------------

# Step 6: Create the 3 Views

Click **+ (Add view)** next to the Jenkins tabs.

Create three views using **List View**.

In each view's configuration, use:

**Use a regular expression to include jobs into the view**

Configure the views as follows:

  View Name        Regex
  ---------------- -------------
  Developer View   `dev-.*`
  Testing View     `test-.*`
  DevOps View      `devops-.*`

This organizes the jobs into separate team-specific views.

------------------------------------------------------------------------

# Step 7: Configure Roles --- The Core of the Assignment

Go to:

**Manage Jenkins → Manage and Assign Roles → Manage Roles**

There are two important types of roles being configured:

1.  **Global Roles** --- control general Jenkins access.
2.  **Item Roles** --- control access to specific jobs using patterns.

------------------------------------------------------------------------

## A. Global Roles --- Controls Login & Basic Jenkins Access

Under **Global roles**, add the following roles:

  Role              Permissions
  ----------------- ----------------------
  `admin`           `Overall/Administer`
  `authenticated`   `Overall/Read`

### `admin`

The `admin` role has:

``` text
Overall → Administer
```

This provides full administrative access.

### `authenticated`

The `authenticated` role has:

``` text
Overall → Read
```

This provides baseline read access to authenticated users so they can
access the Jenkins UI.

Assign `authenticated` to the built-in **authenticated** pseudo-group so
every logged-in user receives baseline read access.

------------------------------------------------------------------------

## B. Item Roles --- Job-Level, Pattern-Based

Still on **Manage Roles**, go to the **Item roles** section.

Add the following roles:

  ------------------------------------------------------------------------------
  Role Name               Pattern (Regex)                Permissions
  ----------------------- ------------------------------ -----------------------
  `dev-role`              `dev-.*`                       Job: Build, Read,
                                                         Workspace, Configure

  `test-role`             `dev-.*\|test-.*`              Job: Build, Read,
                                                         Workspace, Configure

  `devops-role`           `dev-.*\|test-.*\|devops-.*`   Job: Build, Read,
                                                         Workspace, Configure
  ------------------------------------------------------------------------------

The `|` symbol in a regular expression means **OR**.

Therefore:

``` text
dev-.*|test-.*
```

means:

``` text
dev-* OR test-*
```

Similarly:

``` text
dev-.*|test-.*|devops-.*
```

means:

``` text
dev-* OR test-* OR devops-*
```

This is used to represent the required visibility/access relationships
between the teams.

Click **Save**.

------------------------------------------------------------------------

## C. Assign Roles to Users

Go to:

**Manage Jenkins → Manage and Assign Roles → Assign Roles**

### Global Roles Table

Assign:

  Role              User / Group
  ----------------- ------------------------------
  `admin`           `admin-1`
  `authenticated`   Built-in authenticated group

The `authenticated` role should remain assigned to the authenticated
group so that logged-in users have baseline access.

### Item Roles Table

Add each user and assign the corresponding role:

  Item Role       Users
  --------------- ------------------------------
  `dev-role`      `developer-1`, `developer-2`
  `test-role`     `testing-1`, `testing-2`
  `devops-role`   `devops-1`, `devops-2`

Click **Save**.

------------------------------------------------------------------------

# Step 8: Verify

Log out of Jenkins and log in as each user.

Verify that the permissions match the assignment requirements.

## Developer

Test with:

``` text
developer-1
```

Expected access:

-   Sees `dev-*` jobs.
-   Can build `dev-*` jobs.
-   Can configure `dev-*` jobs.
-   Can access the workspace of `dev-*` jobs.
-   Cannot access the Testing and DevOps jobs.

------------------------------------------------------------------------

## Testing

Test with:

``` text
testing-1
```

Expected access:

-   Can see `dev-*` and `test-*` jobs.
-   Can work with `test-*` jobs according to the configured permissions.
-   Developer jobs are visible according to the assignment's requirement
    that Testing can view Developer jobs.

------------------------------------------------------------------------

## DevOps

Test with:

``` text
devops-1
```

Expected access:

-   Can see Developer, Testing, and DevOps jobs.
-   Has the required build/configure/workspace permissions for DevOps
    jobs.
-   Can view Developer and Testing jobs.

------------------------------------------------------------------------

## Administrator

Test with:

``` text
admin-1
```

Expected access:

-   Can see all jobs.
-   Can build and configure jobs.
-   Can access workspaces.
-   Can manage Jenkins users.
-   Can manage roles and permissions.
-   Has full Jenkins administrative access.

------------------------------------------------------------------------

# Assignment Access Model

The final access model can be viewed conceptually as:

``` text
Developer
├── dev-*       → Full job permissions
├── test-*      → No access
└── devops-*    → No access


Testing
├── dev-*       → View
├── test-*      → Full job permissions
└── devops-*    → No access


DevOps
├── dev-*       → View
├── test-*      → View
└── devops-*    → Full job permissions


Admin
├── dev-*       → Full access
├── test-*      → Full access
└── devops-*    → Full access
```

> **Note:** The exact permissions available to each user should be
> verified by logging in as that user and testing the relevant Jenkins
> actions.